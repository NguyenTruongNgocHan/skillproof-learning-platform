package com.skillproof.backend.quiz.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizBankRepository extends JpaRepository<QuizBank, UUID> {
  @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
  @org.springframework.data.jpa.repository.Query("select b from QuizBank b where b.id=:id")
  java.util.Optional<QuizBank> findForUpdate(UUID id);

  List<QuizBank> findByOrganizationIdOrderByCreatedAtDesc(UUID organizationId);

  List<QuizBank> findByCreatedByAndOrganizationIdIsNullOrderByCreatedAtDesc(UUID actor);
}
