package com.skillproof.backend.learning.infrastructure;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface LearningContextRepository extends JpaRepository<LearningPathVersionEntity, UUID> {

    java.util.List<LearningPathVersionEntity> findByPathIdOrderByVersionNoDesc(UUID pathId);

    Optional<LearningPathVersionEntity> findFirstByPathIdAndStatusOrderByVersionNoDesc(UUID pathId, String status);

    interface VersionProjection {

        UUID getId();

        UUID getOrganizationId();

        String getStatus();

        UUID getCompletionPolicyId();
    }

    @Query("""
            select version.id as id, path.organizationId as organizationId,
                   version.status as status, policy.logicalId as completionPolicyId
            from LearningPathVersionEntity version, LearningPathEntity path, CompletionPolicyEntity policy
            where version.id = :versionId and version.pathId = path.id and policy.versionId = version.id
            """)
    Optional<VersionProjection> findCertificationContext(UUID versionId);

    @Query("""
            select version.id as id, path.organizationId as organizationId,
                   version.status as status
            from LearningPathVersionEntity version, LearningPathEntity path
            where version.id = :versionId and version.pathId = path.id
            """)
    Optional<VersionProjection> findVersionContext(UUID versionId);
}
