package com.skillproof.backend.learning.infrastructure;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface LearningContextRepository
    extends JpaRepository<LearningPathVersionEntity, UUID>
{
    @org.springframework.data.jpa.repository.Lock(
        jakarta.persistence.LockModeType.PESSIMISTIC_WRITE
    )
    @Query(
        "select version from LearningPathVersionEntity version where version.id = :id"
    )
    Optional<LearningPathVersionEntity> findForUpdate(UUID id);

    java.util.List<LearningPathVersionEntity> findByPathIdOrderByVersionNoDesc(
        UUID pathId
    );

    Optional<LearningPathVersionEntity> findFirstByPathIdAndStatusOrderByVersionNoDesc(
        UUID pathId,
        String status
    );

    @Query(
        """
        select path from LearningPathEntity path
        where path.id = (select version.pathId from LearningPathVersionEntity version where version.id = :versionId)
        """
    )
    Optional<LearningPathEntity> findPathByVersionId(UUID versionId);

    interface PublishedSourceProjection {
        UUID getId();
        String getTitle();
        int getVersionNo();
    }

    @Query(
        """
        select version.id as id, path.title as title, version.versionNo as versionNo
        from LearningPathVersionEntity version, LearningPathEntity path
        where version.pathId = path.id and path.organizationId = :organizationId
          and version.status = 'PUBLISHED'
        order by path.title
        """
    )
    java.util.List<PublishedSourceProjection> findPublishedSources(
        UUID organizationId
    );

    interface VersionProjection {
        UUID getId();

        UUID getOrganizationId();

        String getStatus();

        UUID getCompletionPolicyId();
    }

    @Query(
        """
        select version.id as id, path.organizationId as organizationId,
               version.status as status, policy.logicalId as completionPolicyId
        from LearningPathVersionEntity version, LearningPathEntity path, CompletionPolicyEntity policy
        where version.id = :versionId and version.pathId = path.id and policy.versionId = version.id
        """
    )
    Optional<VersionProjection> findCertificationContext(UUID versionId);

    @Query(
        """
        select version.id as id, path.organizationId as organizationId,
               version.status as status
        from LearningPathVersionEntity version, LearningPathEntity path
        where version.id = :versionId and version.pathId = path.id
        """
    )
    Optional<VersionProjection> findVersionContext(UUID versionId);
}
