package com.skillproof.backend.identity.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "refresh_token")
public class RefreshToken {

    @Id
    private UUID id;
    private UUID sessionId;
    private String tokenHash;
    private UUID familyId;
    private Instant expiresAt;
    private Instant createdAt;
    private Instant consumedAt;
    private Instant revokedAt;
    private UUID replacedById;

    protected RefreshToken() {
    }

    public static RefreshToken issue(UUID sessionId, UUID familyId, String hash, Instant now, Instant expiry) {
        var token = new RefreshToken();
        token.id = UUID.randomUUID();
        token.sessionId = sessionId;
        token.familyId = familyId;
        token.tokenHash = hash;
        token.createdAt = now;
        token.expiresAt = expiry;
        return token;
    }

    public boolean isUsable(Instant now) {
        return consumedAt == null && revokedAt == null && expiresAt.isAfter(now);
    }

    public boolean wasConsumed() {
        return consumedAt != null;
    }

    public void consume(Instant now, UUID replacementId) {
        consumedAt = now;
        replacedById = replacementId;
    }

    public void revoke(Instant now) {
        revokedAt = now;
    }

    public UUID getId() {
        return id;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public UUID getFamilyId() {
        return familyId;
    }
}
