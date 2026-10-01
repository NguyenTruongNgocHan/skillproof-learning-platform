package com.skillproof.backend.learning.infrastructure;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@Table(name = "resource_progress")
@IdClass(ResourceProgressId.class)
public class ResourceProgressEntity {

    @Id
    @Column(name = "enrollment_id")
    private UUID enrollmentId;

    @Id
    @Column(name = "resource_id")
    private UUID resourceId;

    @Column(name = "completed_at", nullable = false)
    private Instant completedAt;

    protected ResourceProgressEntity() {
    }

    public ResourceProgressEntity(UUID enrollmentId, UUID resourceId, Instant completedAt) {
        this.enrollmentId = enrollmentId;
        this.resourceId = resourceId;
        this.completedAt = completedAt;
    }
}
