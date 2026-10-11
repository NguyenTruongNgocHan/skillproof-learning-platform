package com.skillproof.backend.media.application;

import com.skillproof.backend.media.application.port.MultipartObjectUpload;
import com.skillproof.backend.media.infrastructure.DirectUploadRepository;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class ExpiredMultipartCleanup {
    private final DirectUploadRepository tickets;
    private final MultipartObjectUpload store;

    public ExpiredMultipartCleanup(DirectUploadRepository tickets, MultipartObjectUpload store) {
        this.tickets = tickets;
        this.store = store;
    }

    @Scheduled(fixedDelay = 60000)
    public void cleanup() {
        for (var t :
                tickets
                        .findTop100ByExpiresAtBeforeAndMultipartIdIsNotNullAndAssembledFalseOrderByExpiresAt(
                                Instant.now()))
            try {
                store.abort(t.getStorageKey(), t.getMultipartId());
                t.assembled();
                tickets.save(t);
            } catch (RuntimeException ex) {
                org.slf4j.LoggerFactory.getLogger(getClass())
                        .warn("Could not abort expired multipart ticket {}", t.getId(), ex);
            }
    }
}
