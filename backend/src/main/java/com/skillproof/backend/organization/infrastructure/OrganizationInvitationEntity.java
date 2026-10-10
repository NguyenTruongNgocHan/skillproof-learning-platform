package com.skillproof.backend.organization.infrastructure;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "organization_invitation")
public class OrganizationInvitationEntity {

    @Id
    UUID id;
    @Column(name = "organization_id", nullable = false)
    UUID organizationId;
    @Column(nullable = false, length = 320)
    String email;
    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    String tokenHash;
    @Column(nullable = false, length = 16)
    String status;
    @Column(name = "invited_by", nullable = false)
    UUID invitedBy;
    @Column(name = "accepted_by")
    UUID acceptedBy;
    @Column(name = "expires_at", nullable = false)
    Instant expiresAt;
    @Column(name = "created_at", nullable = false)
    Instant createdAt;
    @Column(name = "accepted_at")
    Instant acceptedAt;

    protected OrganizationInvitationEntity() {
    }

    public OrganizationInvitationEntity(UUID id, UUID organizationId, String email, String tokenHash,
            UUID invitedBy, Instant expiresAt, Instant createdAt) {
        this.id = id;
        this.organizationId = organizationId;
        this.email = email;
        this.tokenHash = tokenHash;
        this.status = "PENDING";
        this.invitedBy = invitedBy;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrganizationId() {
        return organizationId;
    }

    public String getEmail() {
        return email;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public String getStatus() {
        return status;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void accept(UUID userId, Instant at) {
        status = "ACCEPTED";
        acceptedBy = userId;
        acceptedAt = at;
    }

    public void expire() {
        status = "EXPIRED";
    }

    public void revoke() {
        status = "REVOKED";
    }
}
