package com.skillproof.backend.identity;

import com.skillproof.backend.identity.infrastructure.AuthSessionRepository;
import com.skillproof.backend.identity.infrastructure.EmailVerificationTokenRepository;
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
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(IdentityTestMailConfig.class)
class AuthenticationIntegrationTest {

    private static final String EMAIL = "auth@example.com";
    private static final String PASSWORD = "StrongPass@123";

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserAccountRepository users;
    @Autowired EmailVerificationTokenRepository verificationTokens;
    @Autowired AuthSessionRepository sessions;
    @Autowired RefreshTokenRepository refreshTokens;
    @Autowired SecurityAuditEventRepository auditEvents;
    @Autowired CapturingVerificationEmailSender emailSender;

    @BeforeEach
    void cleanDatabase() {
        refreshTokens.deleteAll();
        sessions.deleteAll();
        verificationTokens.deleteAll();
        auditEvents.deleteAll();
        users.deleteAll();
        emailSender.clear();
    }

    @Test
    void verifiedUserCanLoginAndAccessProfile() throws Exception {
        registerAndVerify();

        MvcResult login = login(PASSWORD)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.expiresIn").value(900))
                .andExpect(jsonPath("$.user.email").value(EMAIL))
                .andReturn();

        String accessToken = objectMapper
                .readTree(login.getResponse().getContentAsString())
                .get("accessToken")
                .asText();

        mockMvc.perform(get("/api/v1/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(EMAIL));

        assertThat(login.getResponse().getCookie("skillproof_refresh"))
                .isNotNull();
        assertThat(auditEvents.count()).isEqualTo(1);
    }

    @Test
    void pendingUserCannotLogin() throws Exception {
        register();

        login(PASSWORD)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(
                        "IDENTITY_EMAIL_NOT_VERIFIED"
                ));
    }

    @Test
    void fiveFailedAttemptsPersistAccountLock() throws Exception {
        registerAndVerify();

        for (int attempt = 0; attempt < 5; attempt++) {
            login("WrongPass@123")
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value(
                            "IDENTITY_INVALID_CREDENTIALS"
                    ));
        }

        login(PASSWORD)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(
                        "IDENTITY_ACCOUNT_UNAVAILABLE"
                ));

        assertThat(auditEvents.count()).isEqualTo(5);
    }

    @Test
    void refreshTokenRotatesAndReuseRevokesFamily() throws Exception {
        registerAndVerify();
        Cookie original = login(PASSWORD)
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getCookie("skillproof_refresh");

        MvcResult refreshed = mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(original))
                .andExpect(status().isOk())
                .andReturn();

        Cookie rotated = refreshed.getResponse()
                .getCookie("skillproof_refresh");

        mockMvc.perform(post("/api/v1/auth/refresh").cookie(original))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(
                        "IDENTITY_REFRESH_REUSE"
                ));

        mockMvc.perform(post("/api/v1/auth/refresh").cookie(rotated))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logoutRevokesRefreshSession() throws Exception {
        registerAndVerify();
        Cookie refreshCookie = login(PASSWORD)
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getCookie("skillproof_refresh");

        mockMvc.perform(post("/api/v1/auth/logout")
                        .cookie(refreshCookie))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(refreshCookie))
                .andExpect(status().isUnauthorized());
    }

    private void registerAndVerify() throws Exception {
        register();
        String token = emailSender.latestToken(EMAIL).orElseThrow();
        mockMvc.perform(post("/api/v1/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"token":"%s"}
                                """.formatted(token)))
                .andExpect(status().isOk());
    }

    private void register() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email":"auth@example.com",
                                  "displayName":"Auth Test",
                                  "password":"StrongPass@123"
                                }
                                """))
                .andExpect(status().isCreated());
    }

    private org.springframework.test.web.servlet.ResultActions login(
            String password
    ) throws Exception {
        return mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "email":"auth@example.com",
                          "password":"%s"
                        }
                        """.formatted(password)));
    }
}
