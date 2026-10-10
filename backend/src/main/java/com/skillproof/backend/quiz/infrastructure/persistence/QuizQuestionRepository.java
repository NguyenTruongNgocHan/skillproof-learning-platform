package com.skillproof.backend.quiz.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizQuestionRepository extends JpaRepository<QuizQuestionVersion, UUID> {

    @org.springframework.data.jpa.repository.Query("select distinct q from QuizQuestionVersion q join QuizAssessmentQuestion aq on aq.questionVersion=q join aq.assessment a where a.status= :status order by q.id")
    List<QuizQuestionVersion> published(QuizAssessment.Status status, org.springframework.data.domain.Pageable page);

    @org.springframework.data.jpa.repository.Query("select distinct q from QuizQuestionVersion q join QuizAssessmentQuestion aq on aq.questionVersion=q join aq.assessment a where q.id=:id and a.status= :status")
    Optional<QuizQuestionVersion> published(UUID id, QuizAssessment.Status status);
}
