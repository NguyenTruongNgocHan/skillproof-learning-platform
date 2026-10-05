package com.skillproof.backend.media;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "media_cleanup_task")
class MediaCleanupTask {

    @Id
    UUID id;

    @Column(name = "storage_key", nullable = false)
    UUID storageKey;

    @Column(nullable = false)
    Instant createdAt;

    @Column(name = "next_attempt_at", nullable = false)
    Instant nextAttemptAt;

    @Column(nullable = false)
    int attempts;

    void retryLater() {
        attempts++;
        nextAttemptAt = Instant.now().plusSeconds(
            Math.min(86400L, 30L << Math.min(attempts, 12))
        );
    }

    protected MediaCleanupTask() {}

    MediaCleanupTask(UUID key) {
        id = UUID.randomUUID();
        storageKey = key;
        createdAt = Instant.now();
        nextAttemptAt = createdAt;
    }
}
