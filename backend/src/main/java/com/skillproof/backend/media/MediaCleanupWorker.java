package com.skillproof.backend.media;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
class MediaCleanupWorker {

    private static final Logger log = LoggerFactory.getLogger(
        MediaCleanupWorker.class
    );
    private final MediaCleanupRepository tasks;
    private final MediaAssetRepository assets;
    private final MediaObjectStore objects;

    MediaCleanupWorker(
        MediaCleanupRepository tasks,
        MediaAssetRepository assets,
        MediaObjectStore objects
    ) {
        this.tasks = tasks;
        this.assets = assets;
        this.objects = objects;
    }

    @Scheduled(fixedDelayString = "${skillproof.media.cleanup-delay:30000}")
    public void retry() {
        for (var task : tasks.findByNextAttemptAtLessThanEqualOrderByCreatedAtAsc(
            java.time.Instant.now(),
            PageRequest.of(0, 25)
        )) {
            try {
                if (
                    assets.countByStorageKey(task.storageKey) == 0
                ) objects.delete(task.storageKey);
                tasks.deleteById(task.id);
            } catch (RuntimeException failure) {
                task.retryLater();
                tasks.save(task);
                log.warn(
                    "Media cleanup remains queued for {}",
                    task.storageKey
                );
            }
        }
    }
}
