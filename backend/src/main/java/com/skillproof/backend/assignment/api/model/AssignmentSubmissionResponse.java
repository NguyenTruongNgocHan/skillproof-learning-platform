package com.skillproof.backend.assignment.api.model;

import com.skillproof.backend.assignment.infrastructure.persistence.AssignmentSubmissionEntity;
import java.time.Instant;
import java.util.UUID;

/** Stable HTTP projection; persistence entities are never serialized by this endpoint. */
public record AssignmentSubmissionResponse(
    UUID id,
    UUID assignmentId,
    UUID enrollmentId,
    UUID learnerId,
    String status,
    String body,
    String link,
    Instant createdAt,
    Instant submittedAt,
    Integer scorePercent,
    Boolean passed,
    String feedback,
    UUID gradedBy,
    Instant gradedAt) {
  public static AssignmentSubmissionResponse from(AssignmentSubmissionEntity value) {
    return new AssignmentSubmissionResponse(
        value.getId(),
        value.getAssignmentId(),
        value.getEnrollmentId(),
        value.getLearnerId(),
        value.getStatus(),
        value.getBody(),
        value.getLink(),
        value.getCreatedAt(),
        value.getSubmittedAt(),
        value.getScorePercent(),
        value.getPassed(),
        value.getFeedback(),
        value.getGradedBy(),
        value.getGradedAt());
  }
}
