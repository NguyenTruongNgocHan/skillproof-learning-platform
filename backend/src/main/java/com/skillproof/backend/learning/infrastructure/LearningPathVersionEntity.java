package com.skillproof.backend.learning.infrastructure;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "learning_path_version")
public class LearningPathVersionEntity {

    @Id
    private UUID id;

    @Column(name = "path_id", nullable = false)
    private UUID pathId;

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

    protected LearningPathVersionEntity() {
    }

    public LearningPathVersionEntity(UUID id, UUID pathId, int number, String status, Instant created) {
        this.id = id;
        this.pathId = pathId;
        this.versionNo = number;
        this.status = status;
        this.description = "";
        this.createdAt = created;
    }

    public UUID getId() {
        return id;
    }

    public UUID getPathId() {
        return pathId;
    }

    public int getVersionNo() {
        return versionNo;
    }

    public String getStatus() {
        return status;
    }

    public void publish() {
        status = "PUBLISHED";
        publishedAt = Instant.now();
    }

}
