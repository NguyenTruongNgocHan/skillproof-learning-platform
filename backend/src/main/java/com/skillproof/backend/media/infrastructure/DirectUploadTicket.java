package com.skillproof.backend.media.infrastructure;

import jakarta.persistence.*;

import java.time.*;
import java.util.*;

@Entity
@Table(name = "direct_upload_ticket")
public class DirectUploadTicket {
    @Id private UUID id;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(name = "scope", nullable = false, length = 20)
    private String scope;

    @Column(name = "target_id")
    private UUID targetId;

    @Column(name = "storage_key", nullable = false)
    private UUID storageKey;

    @Column(name = "filename", nullable = false, length = 180)
    private String filename;

    @Column(name = "expected_size", nullable = false)
    private long expectedSize;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "asset_id")
    private UUID assetId;

    @Column(name = "multipart_id", length = 1024)
    private String multipartId;

    @Column(name = "cancelled", nullable = false)
    private boolean cancelled;

    public boolean isCancelled() {
        return cancelled;
    }

    public void cancel() {
        cancelled = true;
    }

    @Column(name = "assembled", nullable = false)
    private boolean assembled;

    public String getMultipartId() {
        return multipartId;
    }

    public boolean isAssembled() {
        return assembled;
    }

    public void multipart(String id) {
        multipartId = id;
    }

    public void assembled() {
        assembled = true;
    }

    protected DirectUploadTicket() {}

    public DirectUploadTicket(
            UUID id,
            UUID ownerId,
            String scope,
            UUID targetId,
            UUID storageKey,
            String filename,
            long expectedSize,
            Instant expiresAt,
            UUID assetId) {
        this.id = id;
        this.ownerId = ownerId;
        this.scope = scope;
        this.targetId = targetId;
        this.storageKey = storageKey;
        this.filename = filename;
        this.expectedSize = expectedSize;
        this.expiresAt = expiresAt;
        this.assetId = assetId;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public String getScope() {
        return scope;
    }

    public UUID getTargetId() {
        return targetId;
    }

    public UUID getStorageKey() {
        return storageKey;
    }

    public String getFilename() {
        return filename;
    }

    public long getExpectedSize() {
        return expectedSize;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public UUID getAssetId() {
        return assetId;
    }

    public void complete(UUID id) {
        assetId = id;
    }
}
