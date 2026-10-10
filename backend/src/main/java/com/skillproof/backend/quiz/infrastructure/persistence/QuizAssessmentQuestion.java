package com.skillproof.backend.quiz.infrastructure.persistence;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "assessment_question")
public class QuizAssessmentQuestion {

    @EmbeddedId
    private QuizAssessmentQuestionId id;
    @ManyToOne
    @MapsId("assessmentId")
    @JoinColumn(name = "assessment_id")
    private QuizAssessment assessment;
    @ManyToOne
    @MapsId("questionVersionId")
    @JoinColumn(name = "question_version_id")
    private QuizQuestionVersion questionVersion;
    private int position;
    private int points;

    public QuizAssessmentQuestionId getId() {
        return id;
    }

    public void setId(QuizAssessmentQuestionId value) {
        id = value;
    }

    public QuizAssessment getAssessment() {
        return assessment;
    }

    public void setAssessment(QuizAssessment value) {
        assessment = value;
    }

    public QuizQuestionVersion getQuestionVersion() {
        return questionVersion;
    }

    public void setQuestionVersion(QuizQuestionVersion value) {
        questionVersion = value;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int value) {
        position = value;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int value) {
        points = value;
    }
}
