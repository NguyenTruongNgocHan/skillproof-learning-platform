package com.skillproof.backend.identity.application;

import java.time.Instant;

public record IssuedEmailVerification(
        String rawToken,
        Instant expiresAt
        ) {

}
