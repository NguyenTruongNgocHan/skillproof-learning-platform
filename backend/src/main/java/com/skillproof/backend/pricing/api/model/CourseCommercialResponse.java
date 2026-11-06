package com.skillproof.backend.pricing.api.model;

import com.skillproof.backend.pricing.infrastructure.CourseCommercial;
import java.time.*;
import java.util.*;

/** Stable HTTP projection; persistence entities are never serialized by this endpoint. */
public record CourseCommercialResponse(
    UUID id,
    UUID organizationId,
    String state,
    String accessMode,
    long priceVnd,
    long certificationPriceVnd,
    Instant updatedAt) {
  public static CourseCommercialResponse from(CourseCommercial value) {
    return new CourseCommercialResponse(
        value.getId(),
        value.getOrganizationId(),
        value.getState(),
        value.getAccessMode(),
        value.getPriceVnd(),
        value.getCertificationPriceVnd(),
        value.getUpdatedAt());
  }
}
