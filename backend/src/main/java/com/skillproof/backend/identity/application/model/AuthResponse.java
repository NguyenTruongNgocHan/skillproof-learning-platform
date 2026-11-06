package com.skillproof.backend.identity.application.model;

import com.skillproof.backend.identity.domain.AccountStatus;
import com.skillproof.backend.identity.domain.UserRole;
import java.util.UUID;

public record AuthResponse(String accessToken, long expiresIn, UserSummary user) {

  public record UserSummary(
      UUID id,
      String email,
      String displayName,
      UserRole role,
      AccountStatus status,
      String onboardingStatus) {
    public UserSummary(
        UUID id, String email, String displayName, UserRole role, AccountStatus status) {
      this(id, email, displayName, role, status, "NOT_STARTED");
    }
  }
}
