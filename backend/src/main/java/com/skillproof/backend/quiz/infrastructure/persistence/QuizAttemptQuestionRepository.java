package com.skillproof.backend.quiz.infrastructure.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizAttemptQuestionRepository extends JpaRepository<QuizAttemptQuestion, QuizAttemptQuestionId> {

    List<QuizAttemptQuestion> findByIdAttemptIdOrderByPosition(UUID attemptId);
}
