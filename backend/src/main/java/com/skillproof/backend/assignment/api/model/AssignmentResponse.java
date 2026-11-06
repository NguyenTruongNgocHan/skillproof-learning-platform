package com.skillproof.backend.assignment.api.model;

import com.skillproof.backend.assignment.infrastructure.persistence.AssignmentEntity;
import java.time.Instant;
import java.util.UUID;

/** Stable HTTP projection; persistence entities are never serialized by this endpoint. */
public record AssignmentResponse(
    UUID id,
    UUID versionId,
    String ownerScope,
    UUID ownerId,
    String title,
    String instructions,
    boolean required,
    int passPercent,
    int maxSubmissions,
    Instant dueAt) {
  public static AssignmentResponse from(AssignmentEntity value) {
    return new AssignmentResponse(
        value.getId(),
        value.getVersionId(),
        value.getOwnerScope(),
        value.getOwnerId(),
        value.getTitle(),
        value.getInstructions(),
        value.getRequired(),
        value.getPassPercent(),
        value.getMaxSubmissions(),
        value.getDueAt());
  }
}
