package com.skillproof.backend.access.domain;

import java.time.Instant;

public final class GrantPolicy {

    private GrantPolicy() {
    }

    public static boolean effective(Instant expiresAt, Instant revokedAt, Instant now) {
        return revokedAt == null && (expiresAt == null || expiresAt.isAfter(now));
    }
}
