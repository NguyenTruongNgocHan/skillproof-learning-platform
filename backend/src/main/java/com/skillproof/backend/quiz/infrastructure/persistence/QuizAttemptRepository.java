package com.skillproof.backend.quiz.infrastructure.persistence;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import jakarta.persistence.LockModeType;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select attempt from QuizAttempt attempt where attempt.id = :id")
    Optional<QuizAttempt> findForUpdate(UUID id);

    List<QuizAttempt> findByAssessmentIdAndEnrollmentIdAndStatus(UUID a, UUID e, QuizAttempt.AttemptStatus s);

    long countByAssessmentIdAndEnrollmentId(UUID a, UUID e);

    List<QuizAttempt> findByEnrollmentIdAndLearnerIdOrderByStartedAtDesc(UUID e, UUID l);

    long countByAssessmentIdInAndEnrollmentIdAndPassedTrue(Collection<UUID> assessmentIds, UUID enrollmentId);

    @Query("""
        select count(distinct attempt.assessmentId)
        from QuizAttempt attempt
        where attempt.assessmentId in :assessmentIds
          and attempt.enrollmentId = :enrollmentId
          and attempt.passed = true
    """)
    long countDistinctPassedAssessments(Collection<UUID> assessmentIds, UUID enrollmentId);

    List<QuizAttempt> findByStatusAndDeadlineAtBefore(QuizAttempt.AttemptStatus status, Instant deadline,
            org.springframework.data.domain.Pageable page);
}
