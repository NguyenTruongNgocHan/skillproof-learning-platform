package com.skillproof.backend.library.infrastructure.persistence;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "library_resource")
public class LibraryResourceEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "author_id", nullable = false)
    private UUID authorId;

    @Column(name = "organization_id", nullable = true)
    private UUID organizationId;

    @Column(name = "title", nullable = false, length = 180)
    private String title;

    @Column(name = "summary", nullable = false, length = 1000)
    private String summary;

    @Column(name = "body", nullable = false, columnDefinition = "text")
    private String body;

    @Column(name = "status", nullable = false, length = 16)
    private String status;

    @Column(name = "access_mode", nullable = false, length = 16)
    private String accessMode = "PUBLIC";

    public String getAccessMode() {
        return accessMode;
    }

    public void configureAccess(String mode) {
        accessMode = mode;
    }

    @Column(name = "price_vnd", nullable = false)
    private long priceVnd;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "reviewed_by", nullable = true)
    private UUID reviewedBy;

    @Column(name = "review_reason", nullable = true, length = 1000)
    private String reviewReason;

    protected LibraryResourceEntity() {
    }

    public LibraryResourceEntity(UUID id, UUID authorId, UUID organizationId, String title, String summary, String body, String status, long priceVnd, Instant createdAt, UUID reviewedBy, String reviewReason) {
        this.id = id;
        this.authorId = authorId;
        this.organizationId = organizationId;
        this.title = title;
        this.summary = summary;
        this.body = body;
        this.status = status;
        this.priceVnd = priceVnd;
        this.createdAt = createdAt;
        this.reviewedBy = reviewedBy;
        this.reviewReason = reviewReason;
    }

    public UUID getId() {
        return id;
    }

    public UUID getAuthorId() {
        return authorId;
    }

    public UUID getOrganizationId() {
        return organizationId;
    }

    public String getTitle() {
        return title;
    }

    public String getSummary() {
        return summary;
    }

    public String getBody() {
        return body;
    }

    public String getStatus() {
        return status;
    }

    public long getPriceVnd() {
        return priceVnd;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public UUID getReviewedBy() {
        return reviewedBy;
    }

    public String getReviewReason() {
        return reviewReason;
    }

    public void edit(String title, String summary, String body, long price) {
        this.title = title;
        this.summary = summary;
        this.body = body;
        priceVnd = price;
    }

    public void submitReview() {
        status = "SUBMITTED";
    }

    public void review(boolean approved, UUID admin, String reason) {
        status = approved ? "PUBLISHED" : "REJECTED";
        reviewedBy = admin;
        reviewReason = reason;
    }

    public void reopenDraft() {
        status = "DRAFT";
        reviewedBy = null;
        reviewReason = null;
    }

}
