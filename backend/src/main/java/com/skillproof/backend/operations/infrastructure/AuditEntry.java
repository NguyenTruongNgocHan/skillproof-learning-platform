package com.skillproof.backend.operations.infrastructure;

import jakarta.persistence.*;

import java.time.*;
import java.util.*;

@Entity
@Table(name = "audit_entry")
public class AuditEntry {
    @Id private UUID id;

    @Column(name = "actor_id", nullable = false)
    private UUID actorId;

    @Column(name = "organization_id")
    private UUID organizationId;

    @Column(name = "action", nullable = false, length = 80)
    private String action;

    @Column(name = "target_id")
    private UUID targetId;

    @Column(name = "details", nullable = false, columnDefinition = "text")
    private String details;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected AuditEntry() {}

    public AuditEntry(
            UUID id,
            UUID actorId,
            UUID organizationId,
            String action,
            UUID targetId,
            String details,
            Instant createdAt) {
        this.id = id;
        this.actorId = actorId;
        this.organizationId = organizationId;
        this.action = action;
        this.targetId = targetId;
        this.details = details;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getActorId() {
        return actorId;
    }

    public UUID getOrganizationId() {
        return organizationId;
    }

    public String getAction() {
        return action;
    }

    public UUID getTargetId() {
        return targetId;
    }

    public String getDetails() {
        return details;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
