package com.skillproof.backend.media.infrastructure.cleanup;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "media_cleanup_task")
public class MediaCleanupTask {

    @Id
    private UUID id;

    @Column(name = "storage_key", nullable = false)
    private UUID storageKey;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(name = "next_attempt_at", nullable = false)
    private Instant nextAttemptAt;

    @Column(nullable = false)
    private int attempts;

    public void retryLater() {
        attempts++;
        nextAttemptAt = Instant.now().plusSeconds(
                Math.min(86400L, 30L << Math.min(attempts, 12))
        );
    }

    protected MediaCleanupTask() {
    }

    public MediaCleanupTask(UUID key) {
        id = UUID.randomUUID();
        storageKey = key;
        createdAt = Instant.now();
        nextAttemptAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID value) {
        id = value;
    }

    public UUID getStorageKey() {
        return storageKey;
    }

    public void setStorageKey(UUID value) {
        storageKey = value;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant value) {
        createdAt = value;
    }

    public Instant getNextAttemptAt() {
        return nextAttemptAt;
    }

    public void setNextAttemptAt(Instant value) {
        nextAttemptAt = value;
    }

    public int getAttempts() {
        return attempts;
    }

    public void setAttempts(int value) {
        attempts = value;
    }
}
