package com.skillproof.backend.assignment.infrastructure.persistence;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssignmentSubmissionRepository
        extends JpaRepository<AssignmentSubmissionEntity, UUID> {

    List<AssignmentSubmissionEntity> findByAssignmentIdAndEnrollmentIdOrderByCreatedAtDesc(
            UUID assignmentId, UUID enrollmentId);

    List<AssignmentSubmissionEntity> findByAssignmentIdOrderByCreatedAtDesc(UUID assignmentId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from AssignmentSubmissionEntity s where s.id = :id")
    Optional<AssignmentSubmissionEntity> lock(UUID id);

    @Query(
            "select count(distinct s.assignmentId) from AssignmentSubmissionEntity s,"
                + " AssignmentEntity a where s.assignmentId = a.id and a.versionId = :versionId and"
                + " a.required = true and s.enrollmentId = :enrollmentId and s.passed = true")
    long countPassed(UUID versionId, UUID enrollmentId);

    @Query(
            "select s from AssignmentSubmissionEntity s,AssignmentEntity a where"
                + " s.assignmentId=a.id and a.versionId in :versions and s.status=:status order by"
                + " s.submittedAt,s.id")
    org.springframework.data.domain.Page<AssignmentSubmissionEntity> queue(
            java.util.Collection<UUID> versions,
            String status,
            org.springframework.data.domain.Pageable page);
}
