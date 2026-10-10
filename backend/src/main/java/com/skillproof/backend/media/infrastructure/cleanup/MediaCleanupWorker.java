package com.skillproof.backend.media.infrastructure.cleanup;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class MediaCleanupWorker {

    private static final Logger LOG = LoggerFactory.getLogger(MediaCleanupWorker.class);
    private final MediaCleanupRepository tasks;
    private final MediaCleanupProcessor processor;

    public MediaCleanupWorker(MediaCleanupRepository tasks, MediaCleanupProcessor processor) {
        this.tasks = tasks;
        this.processor = processor;
    }

    @Scheduled(fixedDelayString = "${skillproof.media.cleanup-delay:30000}")
    public void retry() {
        for (var task : tasks.findByNextAttemptAtLessThanEqualOrderByCreatedAtAsc(Instant.now(), PageRequest.of(0, 25))) {
            try {
                processor.process(task.getId());
            } catch (RuntimeException failure) {
                processor.retryLater(task.getId());
                LOG.warn("Media cleanup remains queued for {}", task.getStorageKey(), failure);
            }
        }
    }
}
