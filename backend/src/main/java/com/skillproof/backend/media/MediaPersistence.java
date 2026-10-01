package com.skillproof.backend.media;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "media_asset")
class MediaAsset {

    @Id
    UUID id;
    @Column(name = "owner_user_id", nullable = false)
    UUID ownerUserId;
    @Column(nullable = false)
    String scope;
    UUID organizationId;
    UUID resourceId;
    @Column(name = "original_name", nullable = false)
    String originalName;
    @Column(name = "storage_key", nullable = false, unique = true)
    UUID storageKey;
    @Column(name = "mime_type", nullable = false)
    String mimeType;
    @Column(name = "size_bytes", nullable = false)
    long sizeBytes;
    @Column(nullable = false, columnDefinition = "char(64)")
    @JdbcTypeCode(SqlTypes.CHAR)
    String sha256;
    @Column(name = "created_at", nullable = false)
    Instant createdAt;
}

interface MediaAssetRepository extends JpaRepository<MediaAsset, UUID> {

    List<MediaAsset> findByScopeAndOwnerUserIdOrderByCreatedAtDesc(String scope, UUID owner);

    List<MediaAsset> findByScopeAndOrganizationIdOrderByCreatedAtDesc(String scope, UUID org);

    List<MediaAsset> findByScopeAndResourceIdOrderByCreatedAtDesc(String scope, UUID resource);

    List<MediaAsset> findByResourceId(UUID resource);

    long countByResourceId(UUID resource);
}
