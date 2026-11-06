package com.skillproof.backend.learningpath.api.model;

import com.skillproof.backend.learningpath.infrastructure.persistence.LearningPathItemEntity;
import java.util.UUID;

/** Stable HTTP projection; persistence entities are never serialized by this endpoint. */
public record LearningPathItemResponse(
    UUID id, UUID pathId, UUID courseVersionId, int position, UUID prerequisiteItemId) {
  public static LearningPathItemResponse from(LearningPathItemEntity value) {
    return new LearningPathItemResponse(
        value.getId(),
        value.getPathId(),
        value.getCourseVersionId(),
        value.getPosition(),
        value.getPrerequisiteItemId());
  }
}
