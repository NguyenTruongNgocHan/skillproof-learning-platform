package com.skillproof.backend.identity.application.model;

import java.time.Instant;
import java.util.UUID;

import com.skillproof.backend.identity.domain.AccountStatus;

public record VerifyEmailResponse(
        UUID id,
        String email,
        String displayName,
        AccountStatus status,
        Instant verifiedAt
        ) {

}
