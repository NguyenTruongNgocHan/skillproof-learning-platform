package com.skillproof.backend.learning.infrastructure;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "enrollment")
public class EnrollmentEntity {

    @Id
    private UUID id;

    @Column(name = "learner_id", nullable = false)
    private UUID learnerId;

    @Column(name = "path_id", nullable = false)
    private UUID pathId;

    @Column(name = "version_id", nullable = false)
    private UUID versionId;

    @Column(nullable = false, length = 12)
    private String status;

    @Column(name = "enrolled_at", nullable = false)
    private Instant enrolledAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    protected EnrollmentEntity() {
    }

    public EnrollmentEntity(UUID id, UUID learnerId, UUID pathId, UUID versionId, String status, Instant enrolledAt) {
        this.id = id;
        this.learnerId = learnerId;
        this.pathId = pathId;
        this.versionId = versionId;
        this.status = status;
        this.enrolledAt = enrolledAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getLearnerId() {
        return learnerId;
    }

    public UUID getVersionId() {
        return versionId;
    }

    public String getStatus() {
        return status;
    }

    void complete(Instant now) {
        status = "COMPLETED";
        if (completedAt == null) {
            completedAt = now;
        }
    }
}
