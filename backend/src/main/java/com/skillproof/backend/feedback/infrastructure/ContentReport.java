package com.skillproof.backend.feedback.infrastructure;

import jakarta.persistence.*;

import java.time.*;
import java.util.*;

@Entity
@Table(name = "content_report")
public class ContentReport {
    @Id private UUID id;

    @Column(name = "actor_id", nullable = false)
    private UUID actorId;

    @Column(name = "review_id", nullable = false)
    private UUID reviewId;

    @Column(name = "reason", nullable = false, length = 2000)
    private String reason;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "resolution", length = 2000)
    private String resolution;

    @Column(name = "moderator_id")
    private UUID moderatorId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected ContentReport() {}

    public ContentReport(
            UUID id,
            UUID actorId,
            UUID reviewId,
            String reason,
            String status,
            String resolution,
            UUID moderatorId,
            Instant createdAt) {
        this.id = id;
        this.actorId = actorId;
        this.reviewId = reviewId;
        this.reason = reason;
        this.status = status;
        this.resolution = resolution;
        this.moderatorId = moderatorId;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getActorId() {
        return actorId;
    }

    public UUID getReviewId() {
        return reviewId;
    }

    public String getReason() {
        return reason;
    }

    public String getStatus() {
        return status;
    }

    public String getResolution() {
        return resolution;
    }

    public UUID getModeratorId() {
        return moderatorId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void resolve(UUID actor, String resolution) {
        status = "RESOLVED";
        moderatorId = actor;
        this.resolution = resolution;
    }
}
