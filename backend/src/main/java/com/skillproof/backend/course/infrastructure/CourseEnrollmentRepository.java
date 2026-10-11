package com.skillproof.backend.course.infrastructure;

import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CourseEnrollmentRepository extends JpaRepository<EnrollmentEntity, UUID> {

    @Query(
            """
            select enrollment from EnrollmentEntity enrollment
            where enrollment.versionId = :versionId
              and (:query = '' or enrollment.learnerId in :learnerIds
                   or cast(enrollment.learnerId as string) like concat('%', :query, '%'))
            order by enrollment.enrolledAt desc
            """)
    Page<EnrollmentEntity> searchByVersionId(
            UUID versionId, String query, java.util.Collection<UUID> learnerIds, Pageable pageable);

    Optional<EnrollmentEntity> findByLearnerIdAndCourseId(UUID learnerId, UUID courseId);

    boolean existsByLearnerIdAndVersionId(UUID learnerId, UUID versionId);

    boolean existsByLearnerIdAndVersionIdAndRetainedFreeAccessTrue(UUID learnerId, UUID versionId);

    List<EnrollmentEntity> findByLearnerIdOrderByEnrolledAtDesc(UUID learnerId);

    List<EnrollmentEntity> findByVersionIdOrderByEnrolledAtDesc(UUID versionId);

    Optional<EnrollmentEntity> findByIdAndLearnerId(UUID id, UUID learnerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select enrollment from EnrollmentEntity enrollment where enrollment.id = :id")
    Optional<EnrollmentEntity> findForCompletion(UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
            """
            select enrollment from EnrollmentEntity enrollment
            where enrollment.id = :id and enrollment.learnerId = :learnerId
            """)
    Optional<EnrollmentEntity> findForUpdate(UUID id, UUID learnerId);

    @Query(
            "select e from EnrollmentEntity e where e.versionId=:version and (:query='' or"
                    + " e.learnerId in :learners or cast(e.learnerId as string) like"
                    + " concat('%',:query,'%')) and (:status='' or e.status=:status) and"
                    + " e.enrolledAt>=:start and e.enrolledAt<:end order by e.enrolledAt desc,e.id")
    Page<EnrollmentEntity> workspaceSearch(
            UUID version,
            String query,
            java.util.Collection<UUID> learners,
            String status,
            java.time.Instant start,
            java.time.Instant end,
            Pageable pageable);
}
