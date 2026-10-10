package com.skillproof.backend.media.infrastructure.persistence;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "media_asset")
public class MediaAssetEntity {

    @Id
    private UUID id;

    @Column(name = "owner_user_id", nullable = false)
    private UUID ownerUserId;

    @Column(nullable = false)
    private String scope;

    private UUID organizationId;
    private UUID resourceId;
    private UUID libraryResourceId;
    private UUID submissionId;

    public UUID getLibraryResourceId() {
        return libraryResourceId;
    }

    public void setLibraryResourceId(UUID value) {
        libraryResourceId = value;
    }

    public UUID getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(UUID value) {
        submissionId = value;
    }

    @Column(name = "original_name", nullable = false)
    private String originalName;

    @Column(name = "storage_key", nullable = false)
    private UUID storageKey;

    @Column(name = "mime_type", nullable = false)
    private String mimeType;

    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;

    @Column(nullable = false, length = 64)
    private String sha256;

    @Column(name = "application_attachment_active", nullable = false)
    private boolean applicationAttachmentActive = true;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID value) {
        id = value;
    }

    public UUID getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(UUID value) {
        ownerUserId = value;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String value) {
        scope = value;
    }

    public UUID getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(UUID value) {
        organizationId = value;
    }

    public UUID getResourceId() {
        return resourceId;
    }

    public void setResourceId(UUID value) {
        resourceId = value;
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String value) {
        originalName = value;
    }

    public UUID getStorageKey() {
        return storageKey;
    }

    public void setStorageKey(UUID value) {
        storageKey = value;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String value) {
        mimeType = value;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }

    public void setSizeBytes(long value) {
        sizeBytes = value;
    }

    public String getSha256() {
        return sha256;
    }

    public void setSha256(String value) {
        sha256 = value;
    }

    public boolean getApplicationAttachmentActive() {
        return applicationAttachmentActive;
    }

    public void setApplicationAttachmentActive(boolean value) {
        applicationAttachmentActive = value;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant value) {
        createdAt = value;
    }
}
