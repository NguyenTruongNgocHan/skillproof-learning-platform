package com.skillproof.backend.course.api.model;

import com.skillproof.backend.course.infrastructure.CourseVersionEntity;
import java.util.UUID;

/** Stable HTTP projection; persistence entities are never serialized by this endpoint. */
public record CourseVersionResponse(
    String accessMode,
    long priceVnd,
    long certificationPriceVnd,
    String reviewStatus,
    String reviewReason,
    String publishedTitle,
    String publishedSummary,
    UUID id,
    UUID courseId,
    int versionNo,
    String status) {
  public static CourseVersionResponse from(CourseVersionEntity value) {
    return new CourseVersionResponse(
        value.getAccessMode(),
        value.getPriceVnd(),
        value.getCertificationPriceVnd(),
        value.getReviewStatus(),
        value.getReviewReason(),
        value.getPublishedTitle(),
        value.getPublishedSummary(),
        value.getId(),
        value.getCourseId(),
        value.getVersionNo(),
        value.getStatus());
  }
}
