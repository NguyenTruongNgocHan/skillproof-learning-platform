package com.skillproof.backend.assignment.infrastructure.persistence;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "submission_criterion_grade")
public class SubmissionCriterionGrade {
    @Id private UUID id;

    @Column(name = "submission_id", nullable = false)
    private UUID submissionId;

    @Column(name = "criterion_id", nullable = false)
    private UUID criterionId;

    @Column(nullable = false)
    private int score;

    @Column(nullable = false, length = 1000)
    private String feedback;

    protected SubmissionCriterionGrade() {}

    public SubmissionCriterionGrade(UUID submission, UUID criterion, int score, String feedback) {
        id = UUID.randomUUID();
        submissionId = submission;
        criterionId = criterion;
        this.score = score;
        this.feedback = feedback;
    }

    public UUID getCriterionId() {
        return criterionId;
    }

    public int getScore() {
        return score;
    }

    public String getFeedback() {
        return feedback;
    }
}
