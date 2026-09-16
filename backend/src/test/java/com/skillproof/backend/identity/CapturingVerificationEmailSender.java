package com.skillproof.backend.identity;

import com.skillproof.backend.identity.infrastructure.email.VerificationEmailSender;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

class CapturingVerificationEmailSender
        implements VerificationEmailSender {

    private final Map<String, String> latestTokens =
            new ConcurrentHashMap<>();

    @Override
    public void sendVerificationEmail(
            String email,
            String displayName,
            String rawToken,
            Instant expiresAt
    ) {

        latestTokens.put(
                email,
                rawToken
        );
    }

    Optional<String> latestToken(
            String email
    ) {

        return Optional.ofNullable(
                latestTokens.get(email)
        );
    }

    void clear() {
        latestTokens.clear();
    }
}