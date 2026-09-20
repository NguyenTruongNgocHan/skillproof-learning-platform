package com.skillproof.backend.identity.api;

import java.util.UUID;

import com.skillproof.backend.identity.domain.AccountStatus;
import com.skillproof.backend.identity.domain.UserRole;

public record AuthResponse(String accessToken, long expiresIn, UserSummary user) {

    public record UserSummary(
            UUID id,
            String email,
            String displayName,
            UserRole role,
            AccountStatus status) {

    }
}
