package com.skillproof.backend.identity;

import com.skillproof.backend.identity.domain.RefreshToken;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RefreshTokenTest {
    @Test
    void issuedTokenIsUsableUntilConsumed() {
        Instant now = Instant.parse("2026-09-16T10:00:00Z");
        RefreshToken token = RefreshToken.issue(UUID.randomUUID(), UUID.randomUUID(), "hash",
                now, now.plusSeconds(60));

        assertThat(token.isUsable(now)).isTrue();
        token.consume(now.plusSeconds(1), UUID.randomUUID());
        assertThat(token.isUsable(now.plusSeconds(2))).isFalse();
        assertThat(token.wasConsumed()).isTrue();
    }

    @Test
    void expiredTokenIsNotUsable() {
        Instant now = Instant.parse("2026-09-16T10:00:00Z");
        RefreshToken token = RefreshToken.issue(UUID.randomUUID(), UUID.randomUUID(), "hash",
                now.minusSeconds(120), now.minusSeconds(60));

        assertThat(token.isUsable(now)).isFalse();
    }
}
