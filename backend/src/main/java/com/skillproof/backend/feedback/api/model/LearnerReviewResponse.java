package com.skillproof.backend.feedback.api.model;

import com.skillproof.backend.feedback.infrastructure.LearnerReview;
import java.time.*;
import java.util.*;

/** Stable HTTP projection; persistence entities are never serialized by this endpoint. */
public record LearnerReviewResponse(
    UUID id,
    UUID actorId,
    UUID organizationId,
    String type,
    UUID targetId,
    int rating,
    String body,
    String status,
    String reply,
    UUID repliedBy,
    String moderationReason,
    Instant createdAt,
    Instant updatedAt) {
  public static LearnerReviewResponse from(LearnerReview value) {
    return new LearnerReviewResponse(
        value.getId(),
        value.getActorId(),
        value.getOrganizationId(),
        value.getType(),
        value.getTargetId(),
        value.getRating(),
        value.getBody(),
        value.getStatus(),
        value.getReply(),
        value.getRepliedBy(),
        value.getModerationReason(),
        value.getCreatedAt(),
        value.getUpdatedAt());
  }
}
