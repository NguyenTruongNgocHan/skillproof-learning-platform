package com.skillproof.backend.feedback.infrastructure;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "review_moderation_entry")
public class ReviewModerationEntry {
    @Id private UUID id;

    @Column(name = "review_id", nullable = false)
    private UUID reviewId;

    @Column(name = "report_id", nullable = false)
    private UUID reportId;

    @Column(name = "actor_id", nullable = false)
    private UUID actorId;

    @Column(nullable = false, length = 20)
    private String decision;

    @Column(nullable = false, length = 1000)
    private String reason;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected ReviewModerationEntry() {}

    public ReviewModerationEntry(
            UUID review, UUID report, UUID actor, boolean hide, String reason) {
        id = UUID.randomUUID();
        reviewId = review;
        reportId = report;
        actorId = actor;
        decision = hide ? "HIDDEN" : "RETAINED";
        this.reason = reason;
        createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getActorId() {
        return actorId;
    }

    public UUID getReportId() {
        return reportId;
    }

    public String getDecision() {
        return decision;
    }

    public String getReason() {
        return reason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
