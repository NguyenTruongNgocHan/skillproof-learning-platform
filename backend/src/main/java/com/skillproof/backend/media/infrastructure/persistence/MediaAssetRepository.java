package com.skillproof.backend.media.infrastructure.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MediaAssetRepository extends JpaRepository<MediaAssetEntity, UUID> {

    List<MediaAssetEntity> findByScopeAndOwnerUserIdOrderByCreatedAtDesc(
            String scope,
            UUID owner
    );

    List<MediaAssetEntity> findByScopeAndOrganizationIdAndApplicationAttachmentActiveTrueOrderByCreatedAtDesc(
            String scope,
            UUID org
    );

    List<MediaAssetEntity> findByScopeAndResourceIdOrderByCreatedAtDesc(
            String scope,
            UUID resource
    );

    List<MediaAssetEntity> findByResourceId(UUID resource);

    long countByResourceId(UUID resource);

    long countByStorageKey(UUID storageKey);

    List<MediaAssetEntity> findByScopeAndLibraryResourceIdOrderByCreatedAtDesc(String scope, UUID libraryResourceId);

    List<MediaAssetEntity> findByScopeAndSubmissionIdOrderByCreatedAtDesc(String scope, UUID submissionId);

    long countBySubmissionId(UUID submissionId);

    long countByLibraryResourceId(UUID libraryResourceId);
}
