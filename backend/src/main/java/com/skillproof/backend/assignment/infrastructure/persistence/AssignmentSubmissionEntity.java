package com.skillproof.backend.assignment.infrastructure.persistence;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "assignment_submission")
public class AssignmentSubmissionEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "assignment_id", nullable = false)
    private UUID assignmentId;

    @Column(name = "enrollment_id", nullable = false)
    private UUID enrollmentId;

    @Column(name = "learner_id", nullable = false)
    private UUID learnerId;

    @Column(name = "status", nullable = false, length = 16)
    private String status;

    @Column(name = "body", nullable = true, columnDefinition = "text")
    private String body;

    @Column(name = "link", nullable = true, length = 1000)
    private String link;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "submitted_at", nullable = true)
    private Instant submittedAt;

    @Column(name = "score_percent", nullable = true)
    private Integer scorePercent;

    @Column(name = "passed", nullable = true)
    private Boolean passed;

    @Column(name = "feedback", nullable = true, length = 2000)
    private String feedback;

    @Column(name = "graded_by", nullable = true)
    private UUID gradedBy;

    @Column(name = "graded_at", nullable = true)
    private Instant gradedAt;

    protected AssignmentSubmissionEntity() {
    }

    public AssignmentSubmissionEntity(UUID id, UUID assignmentId, UUID enrollmentId, UUID learnerId, String status, String body, String link, Instant createdAt, Instant submittedAt, Integer scorePercent, Boolean passed, String feedback, UUID gradedBy, Instant gradedAt) {
        this.id = id;
        this.assignmentId = assignmentId;
        this.enrollmentId = enrollmentId;
        this.learnerId = learnerId;
        this.status = status;
        this.body = body;
        this.link = link;
        this.createdAt = createdAt;
        this.submittedAt = submittedAt;
        this.scorePercent = scorePercent;
        this.passed = passed;
        this.feedback = feedback;
        this.gradedBy = gradedBy;
        this.gradedAt = gradedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getAssignmentId() {
        return assignmentId;
    }

    public UUID getEnrollmentId() {
        return enrollmentId;
    }

    public UUID getLearnerId() {
        return learnerId;
    }

    public String getStatus() {
        return status;
    }

    public String getBody() {
        return body;
    }

    public String getLink() {
        return link;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public Integer getScorePercent() {
        return scorePercent;
    }

    public Boolean getPassed() {
        return passed;
    }

    public String getFeedback() {
        return feedback;
    }

    public UUID getGradedBy() {
        return gradedBy;
    }

    public Instant getGradedAt() {
        return gradedAt;
    }

    public void editDraft(String body, String link) {
        this.body = body;
        this.link = link;
    }

    public void submit(Instant now) {
        status = "SUBMITTED";
        submittedAt = now;
    }

    public void grade(int score, boolean passed, String feedback, UUID grader, Instant now) {
        status = "GRADED";
        scorePercent = score;
        this.passed = passed;
        this.feedback = feedback;
        gradedBy = grader;
        gradedAt = now;
    }
}
