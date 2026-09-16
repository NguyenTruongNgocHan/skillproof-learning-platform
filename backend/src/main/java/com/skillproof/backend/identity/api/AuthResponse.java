package com.skillproof.backend.identity.api;

import com.skillproof.backend.identity.domain.UserRole;

import java.util.UUID;

public record AuthResponse(String accessToken, long expiresIn, UserSummary user) {
    public record UserSummary(UUID id, String email, String displayName, UserRole role) {
    }
}
