package com.skillproof.backend.course.infrastructure;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "course_version")
public class CourseVersionEntity {

    @Id
    private UUID id;

    @Column(name = "course_id", nullable = false)
    private UUID courseId;

    @Column(name = "version_no", nullable = false)
    private int versionNo;

    @Column(nullable = false, length = 12)
    private String status;

    @Column(nullable = false, columnDefinition = "text")
    private String description;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "access_mode", nullable = false, length = 16)
    private String accessMode = "PUBLIC";
    @Column(name = "price_vnd", nullable = false)
    private long priceVnd;
    @Column(name = "certification_price_vnd", nullable = false)
    private long certificationPriceVnd;
    @Column(name = "review_status", nullable = false, length = 16)
    private String reviewStatus = "DRAFT";
    @Column(name = "review_reason", length = 1000)
    private String reviewReason;
    @Column(name = "published_title", nullable = false, length = 180)
    private String publishedTitle = "";
    @Column(name = "published_summary", nullable = false, length = 1000)
    private String publishedSummary = "";

    public String getAccessMode() {
        return accessMode;
    }

    public long getPriceVnd() {
        return priceVnd;
    }

    public long getCertificationPriceVnd() {
        return certificationPriceVnd;
    }

    public String getReviewStatus() {
        return reviewStatus;
    }

    public String getReviewReason() {
        return reviewReason;
    }

    public String getPublishedTitle() {
        return publishedTitle;
    }

    public String getPublishedSummary() {
        return publishedSummary;
    }

    public void configureOffer(String mode, long price, long certificationPrice) {
        accessMode = mode;
        priceVnd = price;
        certificationPriceVnd = certificationPrice;
    }

    public void invalidateReview() {
        reviewStatus = "DRAFT";
        reviewReason = null;
    }

    public void submitReview() {
        reviewStatus = "SUBMITTED";
        reviewReason = null;
    }

    public void review(boolean approved, String reason) {
        reviewStatus = approved ? "APPROVED" : "REJECTED";
        reviewReason = reason;
    }

    public void snapshot(String title, String summary) {
        publishedTitle = title;
        publishedSummary = summary;
    }

    protected CourseVersionEntity() {
    }

    public CourseVersionEntity(
            UUID id,
            UUID courseId,
            int number,
            String status,
            Instant created
    ) {
        this.id = id;
        this.courseId = courseId;
        this.versionNo = number;
        this.status = status;
        this.description = "";
        this.createdAt = created;
    }

    public UUID getId() {
        return id;
    }

    public UUID getCourseId() {
        return courseId;
    }

    public int getVersionNo() {
        return versionNo;
    }

    public String getStatus() {
        return status;
    }

    public void archive() {
        status = "ARCHIVED";
    }

    public void publish() {
        status = "PUBLISHED";
        publishedAt = Instant.now();
    }
}
