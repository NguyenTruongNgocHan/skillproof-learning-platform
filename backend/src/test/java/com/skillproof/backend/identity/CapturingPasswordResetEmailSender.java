package com.skillproof.backend.identity;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.skillproof.backend.identity.infrastructure.email.PasswordResetEmailSender;

class CapturingPasswordResetEmailSender implements PasswordResetEmailSender {

    private final Map<String, String> latestTokens = new ConcurrentHashMap<>();

    @Override
    public void sendPasswordResetEmail(
            String email,
            String displayName,
            String rawToken,
            Instant expiresAt
    ) {
        latestTokens.put(email, rawToken);
    }

    Optional<String> latestToken(String email) {
        return Optional.ofNullable(latestTokens.get(email));
    }

    void clear() {
        latestTokens.clear();
    }
}
