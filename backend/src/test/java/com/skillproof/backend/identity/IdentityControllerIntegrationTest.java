package com.skillproof.backend.identity;

import com.skillproof.backend.identity.infrastructure.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class IdentityControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserAccountRepository userAccountRepository;

    @BeforeEach
    void setUp() {
        userAccountRepository.deleteAll();
    }

    @Test
    void shouldRegisterLearnerSuccessfully() throws Exception {

        String requestBody = """
                {
                  "email": "Learner@Example.com",
                  "password": "StrongPass@123"
                }
                """;

        mockMvc.perform(
                        post("/api/v1/identity/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email")
                        .value("learner@example.com"))
                .andExpect(jsonPath("$.role")
                        .value("LEARNER"))
                .andExpect(jsonPath("$.status")
                        .value("PENDING_VERIFICATION"))
                .andExpect(jsonPath("$.createdAt").exists());

        var account = userAccountRepository
                .findByEmail("learner@example.com")
                .orElseThrow();

        assertThat(account.getEmail())
                .isEqualTo("learner@example.com");

        assertThat(account.getPasswordHash())
                .isNotEqualTo("StrongPass@123");

        assertThat(account.getPasswordHash())
                .startsWith("$2");

        assertThat(userAccountRepository.count())
                .isEqualTo(1);
    }

    @Test
    void shouldRejectWeakPassword() throws Exception {

        String requestBody = """
                {
                  "email": "learner@example.com",
                  "password": "password"
                }
                """;

        mockMvc.perform(
                        post("/api/v1/identity/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code")
                        .value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.password")
                        .exists());

        assertThat(userAccountRepository.count())
                .isZero();
    }

    @Test
    void shouldRejectInvalidEmail() throws Exception {

        String requestBody = """
                {
                  "email": "not-an-email",
                  "password": "StrongPass@123"
                }
                """;

        mockMvc.perform(
                        post("/api/v1/identity/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code")
                        .value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.email")
                        .exists());

        assertThat(userAccountRepository.count())
                .isZero();
    }

    @Test
    void shouldRejectDuplicateEmailIgnoringCase() throws Exception {

        String firstRequest = """
                {
                  "email": "learner@example.com",
                  "password": "StrongPass@123"
                }
                """;

        String duplicateRequest = """
                {
                  "email": "LEARNER@EXAMPLE.COM",
                  "password": "AnotherPass@123"
                }
                """;

        mockMvc.perform(
                        post("/api/v1/identity/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(firstRequest)
                )
                .andExpect(status().isCreated());

        mockMvc.perform(
                        post("/api/v1/identity/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(duplicateRequest)
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code")
                        .value("IDENTITY_EMAIL_ALREADY_EXISTS"));

        assertThat(userAccountRepository.count())
                .isEqualTo(1);
    }
}