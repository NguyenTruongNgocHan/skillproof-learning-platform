package com.skillproof.backend.identity.api;

import com.skillproof.backend.identity.domain.AccountStatus;
import com.skillproof.backend.identity.domain.UserRole;

import java.time.Instant;
import java.util.UUID;

public record ProfileResponse(UUID id, String email, String displayName, UserRole role,
                              AccountStatus status, String headline, String bio, String avatarUrl,
                              String locale, String timezone, Instant emailVerifiedAt) {
}
