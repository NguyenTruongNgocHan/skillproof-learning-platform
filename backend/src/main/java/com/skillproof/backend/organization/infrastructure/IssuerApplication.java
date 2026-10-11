package com.skillproof.backend.organization.infrastructure;

import jakarta.persistence.*;

import java.time.*;
import java.util.*;

@Entity
@Table(name = "issuer_application")
public class IssuerApplication {
    @Column(name = "scope_mode", nullable = false, length = 24)
    private String scopeMode = "ALL_COURSES";

    @Column(name = "course_version_ids", nullable = false, columnDefinition = "text")
    private String courseVersionIds = "";

    public String getScopeMode() {
        return scopeMode;
    }

    public String getCourseVersionIds() {
        return courseVersionIds;
    }

    public void restrictScope(String mode, String ids) {
        scopeMode = mode;
        courseVersionIds = ids;
    }

    @Id private UUID id;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "scope", nullable = false, length = 2000)
    private String scope;

    @Column(name = "evidence_ids", nullable = false, columnDefinition = "text")
    private String evidenceIds;

    @Column(name = "reason", length = 1000)
    private String reason;

    @Column(name = "reviewer_id")
    private UUID reviewerId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @Column(name = "valid_until")
    private Instant validUntil;

    protected IssuerApplication() {}

    public IssuerApplication(
            UUID id,
            UUID organizationId,
            String status,
            String scope,
            String evidenceIds,
            String reason,
            UUID reviewerId,
            Instant createdAt,
            Instant reviewedAt,
            Instant validUntil) {
        this.id = id;
        this.organizationId = organizationId;
        this.status = status;
        this.scope = scope;
        this.evidenceIds = evidenceIds;
        this.reason = reason;
        this.reviewerId = reviewerId;
        this.createdAt = createdAt;
        this.reviewedAt = reviewedAt;
        this.validUntil = validUntil;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrganizationId() {
        return organizationId;
    }

    public String getStatus() {
        return status;
    }

    public String getScope() {
        return scope;
    }

    public String getEvidenceIds() {
        return evidenceIds;
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

    public Instant getValidUntil() {
        return validUntil;
    }

    public void decide(String status, String reason, UUID reviewer, Instant expiry) {
        this.status = status;
        this.reason = reason;
        reviewerId = reviewer;
        reviewedAt = Instant.now();
        validUntil = expiry;
    }
}
