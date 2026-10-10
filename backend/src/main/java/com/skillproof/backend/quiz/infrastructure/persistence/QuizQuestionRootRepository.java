package com.skillproof.backend.quiz.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import jakarta.persistence.LockModeType;

public interface QuizQuestionRootRepository extends JpaRepository<QuizQuestion, UUID> {

    List<QuizQuestion> findByBankIdOrderByCreatedAtDesc(UUID bankId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select question from QuizQuestion question where question.id = :id")
    Optional<QuizQuestion> findForUpdate(UUID id);
}
