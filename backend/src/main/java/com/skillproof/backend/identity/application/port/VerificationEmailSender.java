package com.skillproof.backend.identity.application.port;

import java.time.Instant;

public interface VerificationEmailSender {

    void sendVerificationEmail(
            String email,
            String displayName,
            String rawToken,
            Instant expiresAt
    );
}
