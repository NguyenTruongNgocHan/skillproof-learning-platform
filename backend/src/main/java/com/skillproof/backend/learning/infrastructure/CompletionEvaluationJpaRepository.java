package com.skillproof.backend.learning.infrastructure;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface CompletionEvaluationJpaRepository extends JpaRepository<CompletionEvaluationEntity, UUID> {

    Optional<CompletionEvaluationEntity> findFirstByEnrollmentIdOrderByEvaluatedAtDesc(UUID enrollmentId);
}
