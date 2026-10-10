package com.skillproof.backend.quiz.infrastructure.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizOptionRepository extends JpaRepository<QuizOption, UUID> {

    List<QuizOption> findByQuestionVersionIdOrderByPosition(UUID questionVersionId);
}
