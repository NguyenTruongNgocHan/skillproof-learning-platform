package com.skillproof.backend.media.infrastructure.cleanup;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillproof.backend.media.application.port.MediaObjectStore;
import com.skillproof.backend.media.infrastructure.persistence.MediaAssetRepository;
import com.skillproof.backend.media.infrastructure.persistence.StorageObjectRepository;

@Service
public class MediaCleanupProcessor {

    private final MediaCleanupRepository tasks;
    private final StorageObjectRepository storage;
    private final MediaAssetRepository assets;
    private final MediaObjectStore objects;

    public MediaCleanupProcessor(MediaCleanupRepository tasks, StorageObjectRepository storage, MediaAssetRepository assets, MediaObjectStore objects) {
        this.tasks = tasks;
        this.storage = storage;
        this.assets = assets;
        this.objects = objects;
    }

    @Transactional
    public void process(UUID id) {
        var snapshot = tasks.findById(id);
        if (snapshot.isEmpty()) {
            return;
        }
        // The shared key is locked before any reference can be cloned or uploaded.
        var object = storage.lock(snapshot.get().getStorageKey()).orElseThrow();
        var found = tasks.lock(id);
        if (found.isEmpty()) {
            return;
        }
        if (!object.getDeleted() && assets.countByStorageKey(object.getId()) == 0) {
            objects.delete(object.getId());
            object.markDeleted();
        }
        tasks.delete(found.get());
    }

    @Transactional
    public void retryLater(UUID id) {
        tasks.lock(id).ifPresent(task -> {
            task.retryLater();
            tasks.save(task);
        });
    }
}
