package com.skillproof.backend.identity.api;

import java.time.Instant;
import java.util.UUID;

import com.skillproof.backend.identity.domain.AccountStatus;
import com.skillproof.backend.identity.domain.UserRole;

public record RegisterResponse(
        UUID id,
        String email,
        String displayName,
        UserRole role,
        AccountStatus status,
        Instant createdAt
        ) {

}
