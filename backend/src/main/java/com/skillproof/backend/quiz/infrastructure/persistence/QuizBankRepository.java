package com.skillproof.backend.quiz.infrastructure.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizBankRepository extends JpaRepository<QuizBank, UUID> {

    List<QuizBank> findByOrganizationIdOrderByCreatedAtDesc(UUID organizationId);

    List<QuizBank> findByCreatedByAndOrganizationIdIsNullOrderByCreatedAtDesc(UUID actor);
}
