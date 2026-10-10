package com.skillproof.backend.quiz.infrastructure.persistence;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "assessment_attempt")
public class QuizAttempt {

    @Id
    private UUID id;
    @Column(name = "assessment_id", nullable = false)
    private UUID assessmentId;
    @Column(name = "enrollment_id", nullable = false)
    private UUID enrollmentId;
    @Column(name = "learner_id", nullable = false)
    private UUID learnerId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttemptStatus status;
    @Column(name = "started_at", nullable = false)
    private Instant startedAt;
    @Column(name = "deadline_at", nullable = false)
    private Instant deadlineAt;
    @Column(name = "submitted_at")
    private Instant submittedAt;
    @Column(name = "score_percent")
    private Integer scorePercent;
    private Boolean passed;

    public enum AttemptStatus {
        IN_PROGRESS, SUBMITTED, TIMED_OUT, SCORED
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID value) {
        id = value;
    }

    public UUID getAssessmentId() {
        return assessmentId;
    }

    public void setAssessmentId(UUID value) {
        assessmentId = value;
    }

    public UUID getEnrollmentId() {
        return enrollmentId;
    }

    public void setEnrollmentId(UUID value) {
        enrollmentId = value;
    }

    public UUID getLearnerId() {
        return learnerId;
    }

    public void setLearnerId(UUID value) {
        learnerId = value;
    }

    public AttemptStatus getStatus() {
        return status;
    }

    public void setStatus(AttemptStatus value) {
        status = value;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant value) {
        startedAt = value;
    }

    public Instant getDeadlineAt() {
        return deadlineAt;
    }

    public void setDeadlineAt(Instant value) {
        deadlineAt = value;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(Instant value) {
        submittedAt = value;
    }

    public Integer getScorePercent() {
        return scorePercent;
    }

    public void setScorePercent(Integer value) {
        scorePercent = value;
    }

    public Boolean getPassed() {
        return passed;
    }

    public void setPassed(Boolean value) {
        passed = value;
    }
}
