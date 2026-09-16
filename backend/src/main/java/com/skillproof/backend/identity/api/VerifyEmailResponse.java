package com.skillproof.backend.identity.api;

import com.skillproof.backend.identity.domain.AccountStatus;

import java.time.Instant;
import java.util.UUID;

public record VerifyEmailResponse(
        UUID id,
        String email,
        String displayName,
        AccountStatus status,
        Instant verifiedAt
) {
}