package com.skillproof.backend.freeze;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.skillproof.backend.identity.IdentityTestMailConfig;
import com.skillproof.backend.identity.application.TokenCodec;
import com.skillproof.backend.identity.domain.*;
import com.skillproof.backend.identity.infrastructure.*;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(IdentityTestMailConfig.class)
@Transactional
class FreezeHttpIntegrationTest {
  @Autowired MockMvc http;
  @Autowired UserAccountRepository users;
  @Autowired AuthSessionRepository sessions;
  @Autowired TokenCodec tokens;

  String learnerToken() {
    var now = Instant.now();
    var user =
        UserAccount.newAccount(
            UUID.randomUUID() + "@example.org", "hash", "Learner", UserRole.LEARNER);
    user.verifyEmail(now);
    users.saveAndFlush(user);
    var session =
        sessions.saveAndFlush(
            AuthSession.start(user.getId(), "Test", "127.0.0.1", now, now.plusSeconds(3600)));
    return "Bearer " + tokens.accessToken(user, session.getId(), now);
  }

  @Test
  void catalogSearchIsPublicAndHasStablePaginationEnvelope() throws Exception {
    http.perform(get("/api/v1/courses/search"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.page").value(0));
  }

  @Test
  void invalidPaginationIsAClientError() throws Exception {
    http.perform(get("/api/v1/courses/search?page=-1"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("PAGE_INVALID"));
  }

  @Test
  void learnerContentRequiresAuthentication() throws Exception {
    http.perform(get("/api/v1/courses/me/enrollments/search")).andExpect(status().isUnauthorized());
  }

  @Test
  void learnerCannotUseAdminModerationEndpoint() throws Exception {
    http.perform(
            post("/api/v1/admin/courses/" + UUID.randomUUID() + "/moderation")
                .header("Authorization", learnerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"suspended\":true,\"reason\":\"test\"}"))
        .andExpect(status().isForbidden());
  }

  @Test
  void wrongOrganizationAuthorityIsDenied() throws Exception {
    http.perform(
            get("/api/v1/quiz/organizations/" + UUID.randomUUID() + "/banks")
                .header("Authorization", learnerToken()))
        .andExpect(status().isForbidden());
  }

  @Test
  void profileRejectsInvalidTimezone() throws Exception {
    http.perform(
            patch("/api/v1/me/profile")
                .header("Authorization", learnerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"displayName\":\"Learner\",\"locale\":\"vi-VN\",\"timezone\":\"Not/A_Zone\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("PROFILE_TIMEZONE"));
  }

  @Test
  void malformedUuidIsAClientError() throws Exception {
    http.perform(
            get("/api/v1/courses/versions/not-a-uuid/metadata")
                .header("Authorization", learnerToken()))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("REQUEST_INVALID"));
  }
}
