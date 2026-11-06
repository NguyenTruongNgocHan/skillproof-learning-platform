package com.skillproof.backend.learningpath.api.model;

import com.skillproof.backend.learningpath.infrastructure.persistence.LearningPathEntity;
import java.time.Instant;
import java.util.UUID;

/** Stable HTTP projection; persistence entities are never serialized by this endpoint. */
public record LearningPathResponse(
    UUID id, UUID learnerId, String title, String goal, Instant createdAt) {
  public static LearningPathResponse from(LearningPathEntity value) {
    return new LearningPathResponse(
        value.getId(),
        value.getLearnerId(),
        value.getTitle(),
        value.getGoal(),
        value.getCreatedAt());
  }
}
