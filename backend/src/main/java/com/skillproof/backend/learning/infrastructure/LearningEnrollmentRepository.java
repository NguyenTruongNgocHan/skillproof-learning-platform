package com.skillproof.backend.learning.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import jakarta.persistence.LockModeType;

public interface LearningEnrollmentRepository extends JpaRepository<EnrollmentEntity, UUID> {

    boolean existsByLearnerIdAndVersionId(UUID learnerId, UUID versionId);

    List<EnrollmentEntity> findByLearnerIdOrderByEnrolledAtDesc(UUID learnerId);

    Optional<EnrollmentEntity> findByIdAndLearnerId(UUID id, UUID learnerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select enrollment from EnrollmentEntity enrollment where enrollment.id = :id")
    Optional<EnrollmentEntity> findForCompletion(UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select enrollment from EnrollmentEntity enrollment
            where enrollment.id = :id and enrollment.learnerId = :learnerId
            """)
    Optional<EnrollmentEntity> findForUpdate(UUID id, UUID learnerId);
}
