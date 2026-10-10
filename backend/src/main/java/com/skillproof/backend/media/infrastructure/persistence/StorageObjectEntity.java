package com.skillproof.backend.media.infrastructure.persistence;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "media_storage_object")
public class StorageObjectEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "deleted", nullable = false)
    private boolean deleted;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected StorageObjectEntity() {
    }

    public StorageObjectEntity(UUID id, boolean deleted, Instant createdAt) {
        this.id = id;
        this.deleted = deleted;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public boolean getDeleted() {
        return deleted;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void markDeleted() {
        deleted = true;
    }
}
