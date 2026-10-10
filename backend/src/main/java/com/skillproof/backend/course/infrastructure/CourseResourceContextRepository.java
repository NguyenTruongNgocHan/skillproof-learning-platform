package com.skillproof.backend.course.infrastructure;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CourseResourceContextRepository extends JpaRepository<CourseResourceEntity, UUID> {

    long countByModuleId(UUID moduleId);

    @Query("""
            select count(resource) from CourseResourceEntity resource, CourseModuleEntity module
            where resource.moduleId = module.id and module.versionId = :versionId and resource.requiredForCompletion = true
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
            from CourseResourceEntity resource, CourseModuleEntity module,
                 CourseVersionEntity version, CourseEntity path
            where resource.id = :resourceId and resource.moduleId = module.id
              and module.versionId = version.id and version.courseId = path.id
            """)
    Optional<ResourceProjection> findResourceContext(UUID resourceId);
}
