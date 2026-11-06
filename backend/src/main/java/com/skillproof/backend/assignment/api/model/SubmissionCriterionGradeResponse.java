package com.skillproof.backend.assignment.api.model;

import com.skillproof.backend.assignment.infrastructure.persistence.SubmissionCriterionGrade;
import java.util.UUID;

/** Stable HTTP projection; persistence entities are never serialized by this endpoint. */
public record SubmissionCriterionGradeResponse(UUID criterionId, int score, String feedback) {
  public static SubmissionCriterionGradeResponse from(SubmissionCriterionGrade value) {
    return new SubmissionCriterionGradeResponse(
        value.getCriterionId(), value.getScore(), value.getFeedback());
  }
}
