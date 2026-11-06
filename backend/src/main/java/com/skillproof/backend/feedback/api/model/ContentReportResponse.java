package com.skillproof.backend.feedback.api.model;

import com.skillproof.backend.feedback.infrastructure.ContentReport;
import java.time.*;
import java.util.*;

/** Stable HTTP projection; persistence entities are never serialized by this endpoint. */
public record ContentReportResponse(
    UUID id,
    UUID actorId,
    UUID reviewId,
    String reason,
    String status,
    String resolution,
    UUID moderatorId,
    Instant createdAt) {
  public static ContentReportResponse from(ContentReport value) {
    return new ContentReportResponse(
        value.getId(),
        value.getActorId(),
        value.getReviewId(),
        value.getReason(),
        value.getStatus(),
        value.getResolution(),
        value.getModeratorId(),
        value.getCreatedAt());
  }
}
