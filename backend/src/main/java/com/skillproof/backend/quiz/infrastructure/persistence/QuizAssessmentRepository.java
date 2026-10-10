package com.skillproof.backend.quiz.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface QuizAssessmentRepository extends JpaRepository<QuizAssessment, UUID> {

    List<QuizAssessment> findByVersionIdOrderByCreatedAtDesc(UUID versionId);

    List<QuizAssessment> findByVersionIdAndStatus(UUID versionId, QuizAssessment.Status status);

    boolean existsByVersionIdAndStatus(UUID versionId, QuizAssessment.Status status);

    boolean existsByVersionIdAndKindAndStatus(UUID versionId, QuizAssessment.Kind kind, QuizAssessment.Status status);

    @Query("select assessment.versionId from QuizAssessment assessment where assessment.id = :id")
    Optional<UUID> findVersionId(UUID id);

    boolean existsByOwnerScopeAndOwnerId(String scope, UUID ownerId);
}
