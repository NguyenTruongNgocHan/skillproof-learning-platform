package com.skillproof.backend.media.application;

import com.skillproof.backend.common.exception.*;
import com.skillproof.backend.media.application.port.MultipartObjectUpload;
import com.skillproof.backend.media.infrastructure.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;

@Service
public class MultipartUploadService {
    public static final long PART_SIZE = 8388608L;

    @org.springframework.beans.factory.annotation.Autowired
    private com.skillproof.backend.media.application.port.DirectObjectUpload objectQuery;

    private final DirectUploadService direct;
    private final DirectUploadRepository tickets;
    private final MultipartObjectUpload store;
    private final MediaService media;

    public MultipartUploadService(
            DirectUploadService direct,
            DirectUploadRepository tickets,
            MultipartObjectUpload store,
            MediaService media) {
        this.direct = direct;
        this.tickets = tickets;
        this.store = store;
        this.media = media;
    }

    @Transactional
    public Map<String, Object> prepare(
            UUID actor, String scope, UUID target, String filename, long size) {
        var response = direct.prepare(actor, scope, target, filename, size);
        UUID id = (UUID) response.get("id");
        var t = tickets.lock(id).orElseThrow();
        String upload = store.start(t.getStorageKey());
        org.springframework.transaction.support.TransactionSynchronizationManager
                .registerSynchronization(
                        new org.springframework.transaction.support.TransactionSynchronization() {
                            public void afterCompletion(int status) {
                                if (status != STATUS_COMMITTED)
                                    try {
                                        store.abort(t.getStorageKey(), upload);
                                    } catch (RuntimeException ex) {
                                        org.slf4j.LoggerFactory.getLogger(
                                                        MultipartUploadService.class)
                                                .warn(
                                                        "Multipart compensation failed for ticket"
                                                                + " {}",
                                                        id,
                                                        ex);
                                    }
                            }
                        });
        t.multipart(upload);
        return Map.of("id", id, "partSize", PART_SIZE, "expiresAt", t.getExpiresAt());
    }

    private DirectUploadTicket owned(UUID actor, UUID id) {
        var t =
                tickets.lock(id)
                        .orElseThrow(
                                () ->
                                        new NotFoundException(
                                                "UPLOAD_NOT_FOUND", "Upload not found"));
        if (!actor.equals(t.getOwnerId()))
            throw new org.springframework.security.access.AccessDeniedException(
                    "Upload owner required");
        if (t.isCancelled())
            throw new ConflictException("UPLOAD_CANCELLED", "Upload was cancelled");
        if (t.getMultipartId() == null)
            throw new BadRequestException("UPLOAD_TYPE", "Multipart ticket required");
        if (!t.getExpiresAt().isAfter(Instant.now()))
            throw new ConflictException("UPLOAD_EXPIRED", "Upload expired; choose the file again");
        media.requireUploadAccess(actor, t.getScope(), t.getTargetId());
        return t;
    }

    @Transactional
    public Map<String, Object> status(UUID actor, UUID id) {
        var t = owned(actor, id);
        return Map.of(
                "assembled",
                t.isAssembled(),
                "partSize",
                PART_SIZE,
                "parts",
                t.isAssembled() ? List.of() : store.parts(t.getStorageKey(), t.getMultipartId()));
    }

    @Transactional
    public Map<String, String> part(UUID actor, UUID id, int number) {
        var t = owned(actor, id);
        if (t.isAssembled()
                || number < 1
                || number > (t.getExpectedSize() + PART_SIZE - 1) / PART_SIZE)
            throw new BadRequestException(
                    "UPLOAD_PART", "Invalid part number or upload already assembled");
        return Map.of("url", store.signedPart(t.getStorageKey(), t.getMultipartId(), number));
    }

    @Transactional
    public void assemble(UUID actor, UUID id) {
        var t = owned(actor, id);
        if (t.isAssembled()) return;
        try {
            if (objectQuery.size(t.getStorageKey()) == t.getExpectedSize()) {
                t.assembled();
                return;
            }
        } catch (software.amazon.awssdk.services.s3.model.S3Exception missing) {
            if (missing.statusCode() != 404) throw missing;
        }
        var parts = store.parts(t.getStorageKey(), t.getMultipartId());
        int count = (int) ((t.getExpectedSize() + PART_SIZE - 1) / PART_SIZE);
        if (parts.size() != count)
            throw new BadRequestException("UPLOAD_PARTS", "Upload every part before completion");
        for (int i = 0; i < count; i++) {
            var p = parts.get(i);
            long expected = Math.min(PART_SIZE, t.getExpectedSize() - (long) i * PART_SIZE);
            if (p.number() != i + 1 || p.size() != expected)
                throw new BadRequestException("UPLOAD_PARTS", "Part size mismatch");
        }
        store.assemble(t.getStorageKey(), t.getMultipartId(), parts);
        t.assembled();
    }

    @Transactional
    public void cancel(UUID actor, UUID id) {
        var t = owned(actor, id);
        if (t.getAssetId() != null)
            throw new ConflictException(
                    "UPLOAD_COMPLETE", "Delete a completed attachment through the media API");
        if (!t.isAssembled()) store.abort(t.getStorageKey(), t.getMultipartId());
        t.assembled();
        t.cancel();
    }
}
