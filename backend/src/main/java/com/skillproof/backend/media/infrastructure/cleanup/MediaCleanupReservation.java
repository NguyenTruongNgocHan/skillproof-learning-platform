package com.skillproof.backend.media.infrastructure.cleanup;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MediaCleanupReservation {

    private final com.skillproof.backend.media.infrastructure.persistence.StorageObjectRepository storage;
    private final MediaCleanupRepository tasks;

    public MediaCleanupReservation(MediaCleanupRepository tasks, com.skillproof.backend.media.infrastructure.persistence.StorageObjectRepository storage) {
        this.tasks = tasks;
        this.storage = storage;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void reserve(UUID key) {
        storage.saveAndFlush(new com.skillproof.backend.media.infrastructure.persistence.StorageObjectEntity(key, false, Instant.now()));
        var task = new MediaCleanupTask(key);
        task.setNextAttemptAt(Instant.now().plusSeconds(300));
        tasks.saveAndFlush(task);
    }
}
