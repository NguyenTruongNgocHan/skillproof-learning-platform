package com.skillproof.backend.identity.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "auth_session")
public class AuthSession {

    @Id
    private UUID id;
    private UUID userAccountId;
    private String userAgent;
    private String ipAddress;
    private Instant createdAt;
    private Instant lastSeenAt;
    private Instant expiresAt;
    private Instant revokedAt;

    protected AuthSession() {
    }

    public static AuthSession start(UUID userId, String userAgent, String ip, Instant now, Instant expiresAt) {
        var session = new AuthSession();
        session.id = UUID.randomUUID();
        session.userAccountId = userId;
        session.userAgent = truncate(userAgent, 500);
        session.ipAddress = truncate(ip, 64);
        session.createdAt = now;
        session.lastSeenAt = now;
        session.expiresAt = expiresAt;
        return session;
    }

    public boolean isUsable(Instant now) {
        return revokedAt == null && expiresAt.isAfter(now);
    }

    public void touch(Instant now) {
        lastSeenAt = now;
    }

    public void revoke(Instant now) {
        revokedAt = now;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserAccountId() {
        return userAccountId;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    private static String truncate(String value, int max) {
        return value == null ? null : value.substring(0, Math.min(value.length(), max));
    }
}
