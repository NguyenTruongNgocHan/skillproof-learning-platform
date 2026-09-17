package com.skillproof.backend.identity.application;

import com.skillproof.backend.identity.domain.PasswordResetToken;
import com.skillproof.backend.identity.infrastructure.PasswordResetTokenRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class PasswordResetTokenIssuer {

    private final PasswordResetTokenRepository tokens;
    private final PasswordResetTokenCodec codec;
    private final PasswordResetProperties properties;

    public PasswordResetTokenIssuer(
            PasswordResetTokenRepository tokens,
            PasswordResetTokenCodec codec,
            PasswordResetProperties properties
    ) {
        this.tokens = tokens;
        this.codec = codec;
        this.properties = properties;
    }

    public IssuedPasswordReset issue(UUID userId, Instant now) {
        tokens.invalidateOutstandingTokens(userId, now);
        String rawToken = codec.generateRawToken();
        Instant expiresAt = now.plus(properties.getTtl());
        tokens.saveAndFlush(PasswordResetToken.issue(
                userId,
                codec.hash(rawToken),
                now,
                expiresAt
        ));
        return new IssuedPasswordReset(rawToken, expiresAt);
    }

    public boolean canIssue(UUID userId, Instant now) {
        return tokens.findTopByUserAccountIdOrderByCreatedAtDesc(userId)
                .map(token -> !now.isBefore(
                token.getCreatedAt().plus(properties.getRequestCooldown())
        ))
                .orElse(true);
    }
}
