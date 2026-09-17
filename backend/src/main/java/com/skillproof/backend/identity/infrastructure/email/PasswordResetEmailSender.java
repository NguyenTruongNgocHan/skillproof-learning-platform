package com.skillproof.backend.identity.infrastructure.email;

import java.time.Instant;

public interface PasswordResetEmailSender {

    void sendPasswordResetEmail(
            String email,
            String displayName,
            String rawToken,
            Instant expiresAt
    );
}
