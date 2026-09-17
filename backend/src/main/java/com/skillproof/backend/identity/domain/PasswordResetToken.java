package com.skillproof.backend.identity.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "password_reset_token")
public class PasswordResetToken {

    @Id
    private UUID id;

    @Column(name = "user_account_id", nullable = false)
    private UUID userAccountId;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "consumed_at")
    private Instant consumedAt;

    @Column(name = "invalidated_at")
    private Instant invalidatedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected PasswordResetToken() {
    }

    private PasswordResetToken(
            UUID userAccountId,
            String tokenHash,
            Instant createdAt,
            Instant expiresAt
    ) {
        if (!expiresAt.isAfter(createdAt)) {
            throw new IllegalArgumentException("Reset token expiration must be after creation time.");
        }
        this.id = UUID.randomUUID();
        this.userAccountId = Objects.requireNonNull(userAccountId);
        this.tokenHash = Objects.requireNonNull(tokenHash);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.expiresAt = Objects.requireNonNull(expiresAt);
    }

    public static PasswordResetToken issue(
            UUID userAccountId,
            String tokenHash,
            Instant createdAt,
            Instant expiresAt
    ) {
        return new PasswordResetToken(userAccountId, tokenHash, createdAt, expiresAt);
    }

    public boolean isUsable(Instant now) {
        return consumedAt == null && invalidatedAt == null && expiresAt.isAfter(now);
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserAccountId() {
        return userAccountId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
