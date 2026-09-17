package com.skillproof.backend.identity;

import com.skillproof.backend.identity.infrastructure.AuthSessionRepository;
import com.skillproof.backend.identity.infrastructure.EmailVerificationTokenRepository;
import com.skillproof.backend.identity.infrastructure.PasswordResetTokenRepository;
import com.skillproof.backend.identity.infrastructure.RefreshTokenRepository;
import com.skillproof.backend.identity.infrastructure.SecurityAuditEventRepository;
import com.skillproof.backend.identity.infrastructure.UserAccountRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(IdentityTestMailConfig.class)
class PasswordRecoveryIntegrationTest {

    private static final String EMAIL = "recovery@example.com";
    private static final String OLD_PASSWORD = "StrongPass@123";
    private static final String NEW_PASSWORD = "NewStrongPass@456";

    @Autowired MockMvc mockMvc;
    @Autowired UserAccountRepository users;
    @Autowired EmailVerificationTokenRepository verificationTokens;
    @Autowired PasswordResetTokenRepository resetTokens;
    @Autowired AuthSessionRepository sessions;
    @Autowired RefreshTokenRepository refreshTokens;
    @Autowired SecurityAuditEventRepository auditEvents;
    @Autowired CapturingVerificationEmailSender verificationEmailSender;
    @Autowired CapturingPasswordResetEmailSender resetEmailSender;

    @BeforeEach
    void cleanDatabase() {
        refreshTokens.deleteAll();
        sessions.deleteAll();
        resetTokens.deleteAll();
        verificationTokens.deleteAll();
        auditEvents.deleteAll();
        users.deleteAll();
        verificationEmailSender.clear();
        resetEmailSender.clear();
    }

    @Test
    void forgotPasswordDoesNotRevealWhetherAccountExists() throws Exception {
        forgotPassword("unknown@example.com")
                .andExpect(status().isAccepted());

        registerAndVerify();

        forgotPassword(EMAIL)
                .andExpect(status().isAccepted());

        assertThat(resetEmailSender.latestToken(EMAIL)).isPresent();
        assertThat(resetTokens.count()).isEqualTo(1);
    }

    @Test
    void resetChangesPasswordRevokesSessionAndConsumesToken() throws Exception {
        registerAndVerify();
        Cookie oldRefreshCookie = login(OLD_PASSWORD)
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getCookie("skillproof_refresh");

        forgotPassword(EMAIL).andExpect(status().isAccepted());
        String token = resetEmailSender.latestToken(EMAIL).orElseThrow();

        resetPassword(token, NEW_PASSWORD)
                .andExpect(status().isNoContent());

        login(OLD_PASSWORD).andExpect(status().isUnauthorized());
        login(NEW_PASSWORD).andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/auth/refresh").cookie(oldRefreshCookie))
                .andExpect(status().isUnauthorized());

        resetPassword(token, "AnotherStrong@789")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("IDENTITY_PASSWORD_RESET_INVALID"));
    }

    @Test
    void requestingAgainInvalidatesPreviousToken() throws Exception {
        registerAndVerify();

        forgotPassword(EMAIL).andExpect(status().isAccepted());
        String firstToken = resetEmailSender.latestToken(EMAIL).orElseThrow();
        forgotPassword(EMAIL).andExpect(status().isAccepted());
        String secondToken = resetEmailSender.latestToken(EMAIL).orElseThrow();

        assertThat(secondToken).isNotEqualTo(firstToken);
        resetPassword(firstToken, NEW_PASSWORD)
                .andExpect(status().isBadRequest());
        resetPassword(secondToken, NEW_PASSWORD)
                .andExpect(status().isNoContent());
    }

    @Test
    void weakPasswordIsRejectedBeforeTokenConsumption() throws Exception {
        registerAndVerify();
        forgotPassword(EMAIL).andExpect(status().isAccepted());
        String token = resetEmailSender.latestToken(EMAIL).orElseThrow();

        resetPassword(token, "weak")
                .andExpect(status().isBadRequest());
        resetPassword(token, NEW_PASSWORD)
                .andExpect(status().isNoContent());
    }

    private org.springframework.test.web.servlet.ResultActions forgotPassword(String email)
            throws Exception {
        return mockMvc.perform(post("/api/v1/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"email":"%s"}
                        """.formatted(email)));
    }

    private org.springframework.test.web.servlet.ResultActions resetPassword(
            String token,
            String password
    ) throws Exception {
        return mockMvc.perform(post("/api/v1/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"token":"%s","newPassword":"%s"}
                        """.formatted(token, password)));
    }

    private void registerAndVerify() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email":"recovery@example.com",
                                  "displayName":"Recovery Test",
                                  "password":"StrongPass@123"
                                }
                                """))
                .andExpect(status().isCreated());

        String token = verificationEmailSender.latestToken(EMAIL).orElseThrow();
        mockMvc.perform(post("/api/v1/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"token":"%s"}
                                """.formatted(token)))
                .andExpect(status().isOk());
    }

    private org.springframework.test.web.servlet.ResultActions login(String password)
            throws Exception {
        return mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"email":"recovery@example.com","password":"%s"}
                        """.formatted(password)));
    }
}
