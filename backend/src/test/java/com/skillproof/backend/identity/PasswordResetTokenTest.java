package com.skillproof.backend.identity;

import com.skillproof.backend.identity.domain.PasswordResetToken;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PasswordResetTokenTest {

    @Test
    void issuedTokenIsUsableBeforeExpiration() {
        Instant now = Instant.parse("2026-09-17T10:00:00Z");
        PasswordResetToken token = PasswordResetToken.issue(
                UUID.randomUUID(),
                "a".repeat(64),
                now,
                now.plus(30, ChronoUnit.MINUTES)
        );

        assertThat(token.isUsable(now.plus(29, ChronoUnit.MINUTES))).isTrue();
        assertThat(token.isUsable(now.plus(30, ChronoUnit.MINUTES))).isFalse();
    }

    @Test
    void expirationMustBeAfterCreation() {
        Instant now = Instant.parse("2026-09-17T10:00:00Z");

        assertThatThrownBy(() -> PasswordResetToken.issue(
                UUID.randomUUID(),
                "a".repeat(64),
                now,
                now
        )).isInstanceOf(IllegalArgumentException.class);
    }
}
