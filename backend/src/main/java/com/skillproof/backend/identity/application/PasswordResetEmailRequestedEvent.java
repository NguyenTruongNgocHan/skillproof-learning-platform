package com.skillproof.backend.identity.application;

import java.time.Instant;

public record PasswordResetEmailRequestedEvent(
        String email,
        String displayName,
        String rawToken,
        Instant expiresAt
        ) {

}
