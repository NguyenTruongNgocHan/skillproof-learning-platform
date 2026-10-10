package com.skillproof.backend.course.infrastructure;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CourseContextRepository
        extends JpaRepository<CourseVersionEntity, UUID> {

    @org.springframework.data.jpa.repository.Lock(
            jakarta.persistence.LockModeType.PESSIMISTIC_WRITE
    )
    @Query(
            "select version from CourseVersionEntity version where version.id = :id"
    )
    Optional<CourseVersionEntity> findForUpdate(UUID id);

    java.util.List<CourseVersionEntity> findByStatusAndReviewStatusOrderByCreatedAtAsc(String status, String reviewStatus, org.springframework.data.domain.Pageable pageable);

    java.util.List<CourseVersionEntity> findByCourseIdOrderByVersionNoDesc(
            UUID courseId
    );

    Optional<CourseVersionEntity> findFirstByCourseIdAndStatusOrderByVersionNoDesc(
            UUID courseId,
            String status
    );

    @Query(
            """
        select path from CourseEntity path
        where path.id = (select version.courseId from CourseVersionEntity version where version.id = :versionId)
        """
    )
    Optional<CourseEntity> findCourseByVersionId(UUID versionId);

    interface PublishedSourceProjection {

        UUID getId();

        String getTitle();

        int getVersionNo();
    }

    @Query(
            """
        select version.id as id, version.publishedTitle as title, version.versionNo as versionNo
        from CourseVersionEntity version, CourseEntity path
        where version.courseId = path.id and path.organizationId = :organizationId
          and version.status = 'PUBLISHED'
        order by version.publishedTitle
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
        from CourseVersionEntity version, CourseEntity path, CompletionPolicyEntity policy
        where version.id = :versionId and version.courseId = path.id and policy.versionId = version.id
        """
    )
    Optional<VersionProjection> findCertificationContext(UUID versionId);

    @Query(
            """
        select version.id as id, path.organizationId as organizationId,
               version.status as status
        from CourseVersionEntity version, CourseEntity path
        where version.id = :versionId and version.courseId = path.id
        """
    )
    Optional<VersionProjection> findVersionContext(UUID versionId);
}
