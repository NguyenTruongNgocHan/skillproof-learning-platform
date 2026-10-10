package com.skillproof.backend.quiz.infrastructure.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizAssessmentQuestionRepository extends JpaRepository<QuizAssessmentQuestion, QuizAssessmentQuestionId> {

    List<QuizAssessmentQuestion> findByAssessmentIdOrderByPosition(UUID assessmentId);

    void deleteByAssessmentId(UUID assessmentId);
}
