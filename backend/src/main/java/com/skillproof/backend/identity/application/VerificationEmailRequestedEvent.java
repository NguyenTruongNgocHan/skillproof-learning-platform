package com.skillproof.backend.identity.application;

import java.time.Instant;

public record VerificationEmailRequestedEvent(
        String email,
        String displayName,
        String rawToken,
        Instant expiresAt
        ) {

}
