package com.skillproof.backend.organization.infrastructure;

import java.time.Instant;
import java.util.UUID;

import com.skillproof.backend.organization.domain.Organization;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "organization_review")
public class OrganizationReviewEntity {

    @Id
    private UUID id;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "reviewer_user_id", nullable = false)
    private UUID reviewerUserId;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Organization.Status decision;

    @Column(length = 1000)
    private String reason;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    protected OrganizationReviewEntity() {
    }

    OrganizationReviewEntity(UUID organizationId, UUID reviewerUserId,
            Organization.Status decision, String reason, Instant reviewedAt) {
        this.id = UUID.randomUUID();
        this.organizationId = organizationId;
        this.reviewerUserId = reviewerUserId;
        this.decision = decision;
        this.reason = reason;
        this.reviewedAt = reviewedAt;
    }

    public UUID getReviewerUserId() {
        return reviewerUserId;
    }

    public Organization.Status getDecision() {
        return decision;
    }

    public String getReason() {
        return reason;
    }

    public Instant getReviewedAt() {
        return reviewedAt;
    }
}
