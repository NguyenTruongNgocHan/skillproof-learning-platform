package com.skillproof.backend.learning.infrastructure;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface LearningResourceContextRepository extends JpaRepository<LearningResourceEntity, UUID> {

    long countByModuleId(UUID moduleId);

    @Query("""
            select count(resource) from LearningResourceEntity resource, LearningModuleEntity module
            where resource.moduleId = module.id and module.versionId = :versionId
            """)
    long countByVersionId(UUID versionId);

    interface ResourceProjection {

        UUID getOrganizationId();

        UUID getVersionId();

        String getStatus();

        String getKind();
    }

    @Query("""
            select path.organizationId as organizationId, version.id as versionId,
                   version.status as status, resource.kind as kind
            from LearningResourceEntity resource, LearningModuleEntity module,
                 LearningPathVersionEntity version, LearningPathEntity path
            where resource.id = :resourceId and resource.moduleId = module.id
              and module.versionId = version.id and version.pathId = path.id
            """)
    Optional<ResourceProjection> findResourceContext(UUID resourceId);
}
