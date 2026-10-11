package com.skillproof.backend.feedback.infrastructure;

import jakarta.persistence.*;

import java.time.*;
import java.util.*;

@org.hibernate.annotations.DynamicUpdate
@Entity
@Table(name = "learner_review")
public class LearnerReview {
    @Id private UUID id;

    @Column(name = "actor_id", nullable = false)
    private UUID actorId;

    @Column(name = "organization_id")
    private UUID organizationId;

    @Column(name = "type", nullable = false, length = 20)
    private String type;

    @Column(name = "target_id", nullable = false)
    private UUID targetId;

    @Column(name = "rating", nullable = false)
    private int rating;

    @Column(name = "body", nullable = false, length = 4000)
    private String body;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "reply", length = 2000)
    private String reply;

    @Column(name = "replied_by")
    private UUID repliedBy;

    @Column(name = "moderation_reason", length = 1000)
    private String moderationReason;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected LearnerReview() {}

    public LearnerReview(
            UUID id,
            UUID actorId,
            UUID organizationId,
            String type,
            UUID targetId,
            int rating,
            String body,
            String status,
            String reply,
            UUID repliedBy,
            String moderationReason,
            Instant createdAt,
            Instant updatedAt) {
        this.id = id;
        this.actorId = actorId;
        this.organizationId = organizationId;
        this.type = type;
        this.targetId = targetId;
        this.rating = rating;
        this.body = body;
        this.status = status;
        this.reply = reply;
        this.repliedBy = repliedBy;
        this.moderationReason = moderationReason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public String getType() {
        return type;
    }

    public UUID getTargetId() {
        return targetId;
    }

    public int getRating() {
        return rating;
    }

    public String getBody() {
        return body;
    }

    public String getStatus() {
        return status;
    }

    public String getReply() {
        return reply;
    }

    public UUID getRepliedBy() {
        return repliedBy;
    }

    public String getModerationReason() {
        return moderationReason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void edit(int rating, String body) {
        this.rating = rating;
        this.body = body;
        updatedAt = Instant.now();
    }

    public void respond(UUID actor, String value) {
        repliedBy = actor;
        reply = value;
        updatedAt = Instant.now();
    }

    public void moderate(String status, String reason) {
        this.status = status;
        moderationReason = reason;
        updatedAt = Instant.now();
    }
}
