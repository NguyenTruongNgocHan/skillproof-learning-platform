package com.skillproof.backend.quiz.infrastructure.persistence;

import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Embeddable;

@Embeddable
public class QuizAssessmentQuestionId implements java.io.Serializable {

    private UUID assessmentId;
    private UUID questionVersionId;

    public QuizAssessmentQuestionId() {
    }

    public QuizAssessmentQuestionId(UUID a, UUID q) {
        assessmentId = a;
        questionVersionId = q;
    }

    public boolean equals(Object o) {
        return o instanceof QuizAssessmentQuestionId x && Objects.equals(assessmentId, x.assessmentId) && Objects.equals(questionVersionId, x.questionVersionId);
    }

    public int hashCode() {
        return Objects.hash(assessmentId, questionVersionId);
    }

    public UUID getAssessmentId() {
        return assessmentId;
    }

    public void setAssessmentId(UUID value) {
        assessmentId = value;
    }

    public UUID getQuestionVersionId() {
        return questionVersionId;
    }

    public void setQuestionVersionId(UUID value) {
        questionVersionId = value;
    }
}
