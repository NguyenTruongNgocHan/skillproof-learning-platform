package com.skillproof.backend.learningpath.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import jakarta.persistence.LockModeType;

public interface LearningPathRepository extends JpaRepository<LearningPathEntity, UUID> {

    List<LearningPathEntity> findByLearnerIdOrderByCreatedAtDesc(UUID learnerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from LearningPathEntity p where p.id = :id and p.learnerId = :learner")
    Optional<LearningPathEntity> lock(UUID id, UUID learner);

}
