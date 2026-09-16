package com.skillproof.backend.identity.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "email_verification_token")
public class EmailVerificationToken {

    @Id
    private UUID id;

    @Column(
            name = "user_account_id",
            nullable = false
    )
    private UUID userAccountId;

    @Column(
            name = "token_hash",
            nullable = false,
            unique = true,
            length = 64
    )
    private String tokenHash;

    @Column(
            name = "expires_at",
            nullable = false
    )
    private Instant expiresAt;

    @Column(name = "consumed_at")
    private Instant consumedAt;

    @Column(name = "invalidated_at")
    private Instant invalidatedAt;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    protected EmailVerificationToken() {
    }

    private EmailVerificationToken(
            UUID id,
            UUID userAccountId,
            String tokenHash,
            Instant expiresAt,
            Instant createdAt
    ) {
        this.id = Objects.requireNonNull(id);
        this.userAccountId = Objects.requireNonNull(userAccountId);
        this.tokenHash = Objects.requireNonNull(tokenHash);
        this.expiresAt = Objects.requireNonNull(expiresAt);
        this.createdAt = Objects.requireNonNull(createdAt);

        if (!expiresAt.isAfter(createdAt)) {
            throw new IllegalArgumentException(
                    "Verification token expiration must be after creation time."
            );
        }
    }

    public static EmailVerificationToken issue(
            UUID userAccountId,
            String tokenHash,
            Instant createdAt,
            Instant expiresAt
    ) {
        return new EmailVerificationToken(
                UUID.randomUUID(),
                userAccountId,
                tokenHash,
                expiresAt,
                createdAt
        );
    }

    public boolean isExpired(Instant now) {
        return !expiresAt.isAfter(now);
    }

    public boolean isConsumed() {
        return consumedAt != null;
    }

    public boolean isInvalidated() {
        return invalidatedAt != null;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserAccountId() {
        return userAccountId;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getConsumedAt() {
        return consumedAt;
    }

    public Instant getInvalidatedAt() {
        return invalidatedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
