package com.skillproof.backend.feedback.infrastructure;

import jakarta.persistence.*;

import java.time.*;
import java.util.*;

@Entity
@Table(name = "experience_receipt")
public class ExperienceReceipt {
    @Id private UUID id;

    @Column(name = "actor_id", nullable = false)
    private UUID actorId;

    @Column(name = "type", nullable = false, length = 20)
    private String type;

    @Column(name = "target_id", nullable = false)
    private UUID targetId;

    @Column(name = "accessed_at", nullable = false)
    private Instant accessedAt;

    protected ExperienceReceipt() {}

    public ExperienceReceipt(
            UUID id, UUID actorId, String type, UUID targetId, Instant accessedAt) {
        this.id = id;
        this.actorId = actorId;
        this.type = type;
        this.targetId = targetId;
        this.accessedAt = accessedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getActorId() {
        return actorId;
    }

    public String getType() {
        return type;
    }

    public UUID getTargetId() {
        return targetId;
    }

    public Instant getAccessedAt() {
        return accessedAt;
    }
}
