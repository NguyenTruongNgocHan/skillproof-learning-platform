package com.skillproof.backend.assignment.api.model;

import com.skillproof.backend.assignment.infrastructure.persistence.AssignmentRubricCriterion;
import java.util.UUID;

/** Stable HTTP projection; persistence entities are never serialized by this endpoint. */
public record AssignmentRubricCriterionResponse(
    UUID id, UUID assignmentId, String title, int weight, int position) {
  public static AssignmentRubricCriterionResponse from(AssignmentRubricCriterion value) {
    return new AssignmentRubricCriterionResponse(
        value.getId(),
        value.getAssignmentId(),
        value.getTitle(),
        value.getWeight(),
        value.getPosition());
  }
}
