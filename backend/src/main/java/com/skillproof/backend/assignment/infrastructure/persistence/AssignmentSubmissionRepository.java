package com.skillproof.backend.assignment.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import jakarta.persistence.LockModeType;

public interface AssignmentSubmissionRepository extends JpaRepository<AssignmentSubmissionEntity, UUID> {

    List<AssignmentSubmissionEntity> findByAssignmentIdAndEnrollmentIdOrderByCreatedAtDesc(UUID assignmentId, UUID enrollmentId);

    List<AssignmentSubmissionEntity> findByAssignmentIdOrderByCreatedAtDesc(UUID assignmentId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from AssignmentSubmissionEntity s where s.id = :id")
    Optional<AssignmentSubmissionEntity> lock(UUID id);

    @Query("select count(distinct s.assignmentId) from AssignmentSubmissionEntity s, AssignmentEntity a where s.assignmentId = a.id and a.versionId = :versionId and a.required = true and s.enrollmentId = :enrollmentId and s.passed = true")
    long countPassed(UUID versionId, UUID enrollmentId);

}
