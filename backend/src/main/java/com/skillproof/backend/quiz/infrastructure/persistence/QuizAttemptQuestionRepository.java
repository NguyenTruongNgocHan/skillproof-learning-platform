package com.skillproof.backend.quiz.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface QuizAttemptQuestionRepository
        extends JpaRepository<QuizAttemptQuestion, QuizAttemptQuestionId> {

    @org.springframework.data.jpa.repository.Query(
            value =
                    "select count(*) from attempt_question q join assessment_attempt a on"
                            + " a.id=q.attempt_id where q.question_version_id=:version and"
                            + " a.learner_id=:actor and a.status IN ('SCORED','TIMED_OUT')",
            nativeQuery = true)
    long experienced(UUID actor, UUID version);

    List<QuizAttemptQuestion> findByIdAttemptIdOrderByPosition(UUID attemptId);
}
