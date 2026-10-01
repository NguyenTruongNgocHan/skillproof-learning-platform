package com.skillproof.backend.organization.infrastructure;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "organization_membership")
public class OrganizationMembershipEntity {

    @Id
    private UUID id;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    private boolean active;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected OrganizationMembershipEntity() {
    }

    OrganizationMembershipEntity(UUID organizationId, UUID userId, Instant createdAt) {
        this.id = UUID.randomUUID();
        this.organizationId = organizationId;
        this.userId = userId;
        this.active = true;
        this.createdAt = createdAt;
    }

    UUID id() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public boolean isActive() {
        return active;
    }

    void deactivate() {
        active = false;
    }
}
