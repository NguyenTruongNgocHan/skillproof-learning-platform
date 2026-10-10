package com.skillproof.backend.quiz.infrastructure.persistence;

import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Embeddable;

@Embeddable
public class QuizAttemptQuestionId implements java.io.Serializable {

    private UUID attemptId;
    private UUID questionVersionId;

    public QuizAttemptQuestionId() {
    }

    public QuizAttemptQuestionId(UUID a, UUID q) {
        attemptId = a;
        questionVersionId = q;
    }

    public boolean equals(Object o) {
        return o instanceof QuizAttemptQuestionId x && Objects.equals(attemptId, x.attemptId) && Objects.equals(questionVersionId, x.questionVersionId);
    }

    public int hashCode() {
        return Objects.hash(attemptId, questionVersionId);
    }

    public UUID getAttemptId() {
        return attemptId;
    }

    public void setAttemptId(UUID value) {
        attemptId = value;
    }

    public UUID getQuestionVersionId() {
        return questionVersionId;
    }

    public void setQuestionVersionId(UUID value) {
        questionVersionId = value;
    }
}
