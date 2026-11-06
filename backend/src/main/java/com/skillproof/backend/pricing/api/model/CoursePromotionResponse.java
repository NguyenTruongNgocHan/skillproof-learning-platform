package com.skillproof.backend.pricing.api.model;

import com.skillproof.backend.pricing.infrastructure.CoursePromotion;
import java.time.*;
import java.util.*;

/** Stable HTTP projection; persistence entities are never serialized by this endpoint. */
public record CoursePromotionResponse(
    UUID id,
    UUID courseId,
    String name,
    int percent,
    Instant startsAt,
    Instant endsAt,
    boolean active,
    UUID createdBy) {
  public static CoursePromotionResponse from(CoursePromotion value) {
    return new CoursePromotionResponse(
        value.getId(),
        value.getCourseId(),
        value.getName(),
        value.getPercent(),
        value.getStartsAt(),
        value.getEndsAt(),
        value.getActive(),
        value.getCreatedBy());
  }
}
