package com.skillproof.backend.feedback.api.model;

import com.skillproof.backend.feedback.infrastructure.ReviewModerationEntry;
import java.time.Instant;
import java.util.UUID;

/** Stable HTTP projection; persistence entities are never serialized by this endpoint. */
public record ReviewModerationEntryResponse(
    UUID id, UUID actorId, UUID reportId, String decision, String reason, Instant createdAt) {
  public static ReviewModerationEntryResponse from(ReviewModerationEntry value) {
    return new ReviewModerationEntryResponse(
        value.getId(),
        value.getActorId(),
        value.getReportId(),
        value.getDecision(),
        value.getReason(),
        value.getCreatedAt());
  }
}
