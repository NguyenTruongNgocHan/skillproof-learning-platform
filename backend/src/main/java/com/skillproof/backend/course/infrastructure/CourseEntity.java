package com.skillproof.backend.course.infrastructure;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "course")
public class CourseEntity {

    @Id
    private UUID id;

    @Column(name = "organization_id")
    private UUID organizationId;

    @Column(nullable = false, length = 100)
    private String slug;

    @Column(nullable = false, length = 180)
    private String title;

    @Column(nullable = false, length = 1000)
    private String summary;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected CourseEntity() {
    }

    public CourseEntity(UUID id, UUID organizationId, String slug, String title, String summary, UUID createdBy, Instant createdAt) {
        this.id = id;
        this.organizationId = organizationId;
        this.slug = slug;
        this.title = title;
        this.summary = summary;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrganizationId() {
        return organizationId;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public String getSlug() {
        return slug;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getTitle() {
        return title;
    }

    public String getSummary() {
        return summary;
    }

    public void edit(String title, String summary) {
        this.title = title;
        this.summary = summary;
    }

}
