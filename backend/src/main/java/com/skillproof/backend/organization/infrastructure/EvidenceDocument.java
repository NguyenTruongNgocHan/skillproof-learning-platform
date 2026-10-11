package com.skillproof.backend.organization.infrastructure;

import jakarta.persistence.*;

import java.time.*;
import java.util.*;

@Entity
@Table(name = "organization_evidence")
public class EvidenceDocument {
    @Id private UUID id;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "media_id", nullable = false)
    private UUID mediaId;

    @Column(name = "kind", nullable = false, length = 40)
    private String kind;

    @Column(name = "purpose", nullable = false, length = 20)
    private String purpose;

    @Column(name = "title", nullable = false, length = 180)
    private String title;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "reason", length = 1000)
    private String reason;

    @Column(name = "reviewer_id")
    private UUID reviewerId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    protected EvidenceDocument() {}

    public EvidenceDocument(
            UUID id,
            UUID organizationId,
            UUID mediaId,
            String kind,
            String purpose,
            String title,
            Instant expiresAt,
            String status,
            String reason,
            UUID reviewerId,
            Instant createdAt,
            Instant reviewedAt) {
        this.id = id;
        this.organizationId = organizationId;
        this.mediaId = mediaId;
        this.kind = kind;
        this.purpose = purpose;
        this.title = title;
        this.expiresAt = expiresAt;
        this.status = status;
        this.reason = reason;
        this.reviewerId = reviewerId;
        this.createdAt = createdAt;
        this.reviewedAt = reviewedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrganizationId() {
        return organizationId;
    }

    public UUID getMediaId() {
        return mediaId;
    }

    public String getKind() {
        return kind;
    }

    public String getPurpose() {
        return purpose;
    }

    public String getTitle() {
        return title;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public String getStatus() {
        return status;
    }

    public String getReason() {
        return reason;
    }

    public UUID getReviewerId() {
        return reviewerId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getReviewedAt() {
        return reviewedAt;
    }

    public void review(String status, String reason, UUID actor) {
        this.status = status;
        this.reason = reason;
        this.reviewerId = actor;
        this.reviewedAt = Instant.now();
    }
}
