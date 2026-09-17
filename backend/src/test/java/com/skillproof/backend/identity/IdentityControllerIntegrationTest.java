package com.skillproof.backend.identity;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.skillproof.backend.identity.application.EmailVerificationTokenCodec;
import com.skillproof.backend.identity.domain.AccountStatus;
import com.skillproof.backend.identity.domain.EmailVerificationToken;
import com.skillproof.backend.identity.infrastructure.EmailVerificationTokenRepository;
import com.skillproof.backend.identity.infrastructure.PasswordResetTokenRepository;
import com.skillproof.backend.identity.infrastructure.UserAccountRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(IdentityTestMailConfig.class)
class IdentityControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserAccountRepository userAccountRepository;

    @Autowired
    EmailVerificationTokenRepository tokenRepository;

    @Autowired
    PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    CapturingVerificationEmailSender emailSender;

    @Autowired
    EmailVerificationTokenCodec tokenCodec;

    @BeforeEach
    void setUp() {

        tokenRepository.deleteAll();

        passwordResetTokenRepository.deleteAll();

        userAccountRepository.deleteAll();

        emailSender.clear();
    }

    @Test
    void shouldRegisterLearnerSuccessfully()
            throws Exception {

        register(
                "Learner@Example.com",
                "Learner One",
                "StrongPass@123"
        )
                .andExpect(status().isCreated())
                .andExpect(
                        jsonPath("$.id").exists()
                )
                .andExpect(
                        jsonPath("$.email")
                                .value(
                                        "learner@example.com"
                                )
                )
                .andExpect(
                        jsonPath("$.displayName")
                                .value("Learner One")
                )
                .andExpect(
                        jsonPath("$.role")
                                .value("LEARNER")
                )
                .andExpect(
                        jsonPath("$.status")
                                .value(
                                        "PENDING_VERIFICATION"
                                )
                );

        var account =
                userAccountRepository
                        .findByEmail(
                                "learner@example.com"
                        )
                        .orElseThrow();

        assertThat(account.getPasswordHash())
                .doesNotContain(
                        "StrongPass@123"
                );

        assertThat(account.getPasswordHash())
                .startsWith("$2");

        assertThat(
                emailSender.latestToken(
                        "learner@example.com"
                )
        ).isPresent();

        assertThat(tokenRepository.count())
                .isEqualTo(1);
    }

    @Test
    void shouldRejectWeakPassword()
            throws Exception {

        register(
                "learner@example.com",
                "Learner One",
                "password"
        )
                .andExpect(
                        status().isBadRequest()
                )
                .andExpect(
                        jsonPath("$.code")
                                .value(
                                        "VALIDATION_ERROR"
                                )
                );

        assertThat(
                userAccountRepository.count()
        ).isZero();
    }

    @Test
    void shouldRejectBlankDisplayName()
            throws Exception {

        register(
                "learner@example.com",
                " ",
                "StrongPass@123"
        )
                .andExpect(
                        status().isBadRequest()
                )
                .andExpect(
                        jsonPath(
                                "$.fieldErrors.displayName"
                        ).exists()
                );
    }

    @Test
    void shouldRejectDuplicateEmailIgnoringCase()
            throws Exception {

        register(
                "learner@example.com",
                "Learner One",
                "StrongPass@123"
        )
                .andExpect(
                        status().isCreated()
                );

        register(
                "LEARNER@EXAMPLE.COM",
                "Another Learner",
                "AnotherPass@123"
        )
                .andExpect(
                        status().isConflict()
                )
                .andExpect(
                        jsonPath("$.code")
                                .value(
                                        "IDENTITY_ALREADY_EXISTS"
                                )
                );

        assertThat(
                userAccountRepository.count()
        ).isEqualTo(1);
    }

    @Test
    void validVerificationTokenShouldActivateAccount()
            throws Exception {

        registerDefault()
                .andExpect(
                        status().isCreated()
                );

        String token =
                emailSender.latestToken(
                                "learner@example.com"
                        )
                        .orElseThrow();

        verify(token)
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.email")
                                .value(
                                        "learner@example.com"
                                )
                )
                .andExpect(
                        jsonPath("$.status")
                                .value("ACTIVE")
                )
                .andExpect(
                        jsonPath("$.verifiedAt")
                                .exists()
                );

        var account =
                userAccountRepository
                        .findByEmail(
                                "learner@example.com"
                        )
                        .orElseThrow();

        assertThat(account.getStatus())
                .isEqualTo(
                        AccountStatus.ACTIVE
                );

        assertThat(
                account.getEmailVerifiedAt()
        ).isNotNull();
    }

    @Test
    void invalidVerificationTokenShouldBeRejected()
            throws Exception {

        String invalidToken =
                "invalid-verification-token-"
                        + UUID.randomUUID();

        verify(invalidToken)
                .andExpect(
                        status().isBadRequest()
                )
                .andExpect(
                        jsonPath("$.code")
                                .value(
                                        "IDENTITY_VERIFICATION_INVALID"
                                )
                );
    }

    @Test
    void expiredVerificationTokenShouldBeRejected()
            throws Exception {

        registerDefault()
                .andExpect(
                        status().isCreated()
                );

        var account =
                userAccountRepository
                        .findByEmail(
                                "learner@example.com"
                        )
                        .orElseThrow();

        tokenRepository.deleteAll();

        String rawToken =
                "expired-verification-token-"
                        + UUID.randomUUID();

        Instant createdAt =
                Instant.now()
                        .minus(
                                Duration.ofHours(1)
                        );

        Instant expiresAt =
                Instant.now()
                        .minus(
                                Duration.ofMinutes(30)
                        );

        EmailVerificationToken expiredToken =
                EmailVerificationToken.issue(
                        account.getId(),
                        tokenCodec.hash(rawToken),
                        createdAt,
                        expiresAt
                );

        tokenRepository.saveAndFlush(
                expiredToken
        );

        verify(rawToken)
                .andExpect(
                        status().isBadRequest()
                )
                .andExpect(
                        jsonPath("$.code")
                                .value(
                                        "IDENTITY_VERIFICATION_EXPIRED"
                                )
                );
    }

    @Test
    void verificationTokenCannotBeReused()
            throws Exception {

        registerDefault()
                .andExpect(
                        status().isCreated()
                );

        String token =
                emailSender.latestToken(
                                "learner@example.com"
                        )
                        .orElseThrow();

        verify(token)
                .andExpect(
                        status().isOk()
                );

        verify(token)
                .andExpect(
                        status().isConflict()
                )
                .andExpect(
                        jsonPath("$.code")
                                .value(
                                        "IDENTITY_ALREADY_VERIFIED"
                                )
                );
    }

    @Test
    void resendShouldInvalidatePreviousToken()
            throws Exception {

        registerDefault()
                .andExpect(
                        status().isCreated()
                );

        String firstToken =
                emailSender.latestToken(
                                "learner@example.com"
                        )
                        .orElseThrow();

        resend(
                "learner@example.com"
        )
                .andExpect(
                        status().isAccepted()
                );

        String secondToken =
                emailSender.latestToken(
                                "learner@example.com"
                        )
                        .orElseThrow();

        assertThat(secondToken)
                .isNotEqualTo(firstToken);

        verify(firstToken)
                .andExpect(
                        status().isBadRequest()
                )
                .andExpect(
                        jsonPath("$.code")
                                .value(
                                        "IDENTITY_VERIFICATION_INVALID"
                                )
                );

        verify(secondToken)
                .andExpect(
                        status().isOk()
                );
    }

    @Test
    void resendForUnknownEmailShouldStillReturnAccepted()
            throws Exception {

        resend(
                "unknown@example.com"
        )
                .andExpect(
                        status().isAccepted()
                );

        assertThat(
                emailSender.latestToken(
                        "unknown@example.com"
                )
        ).isEmpty();
    }

    private org.springframework.test.web.servlet.ResultActions
    registerDefault()
            throws Exception {

        return register(
                "learner@example.com",
                "Learner One",
                "StrongPass@123"
        );
    }

    private org.springframework.test.web.servlet.ResultActions
    register(
            String email,
            String displayName,
            String password
    ) throws Exception {

        String body = """
                {
                  "email": "%s",
                  "displayName": "%s",
                  "password": "%s"
                }
                """
                .formatted(
                        email,
                        displayName,
                        password
                );

        return mockMvc.perform(
                post("/api/v1/auth/register")
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .content(body)
        );
    }

    private org.springframework.test.web.servlet.ResultActions
    verify(
            String token
    ) throws Exception {

        String body = """
                {
                  "token": "%s"
                }
                """
                .formatted(token);

        return mockMvc.perform(
                post(
                        "/api/v1/auth/verify-email"
                )
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .content(body)
        );
    }

    private org.springframework.test.web.servlet.ResultActions
    resend(
            String email
    ) throws Exception {

        String body = """
                {
                  "email": "%s"
                }
                """
                .formatted(email);

        return mockMvc.perform(
                post(
                        "/api/v1/auth/resend-verification"
                )
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .content(body)
        );
    }
}
