package com.skillproof.backend.organization.api.model;

import com.skillproof.backend.organization.infrastructure.IssuerApplication;
import java.time.*;
import java.util.*;

/** Stable HTTP projection; persistence entities are never serialized by this endpoint. */
public record IssuerApplicationResponse(
    String scopeMode,
    String courseVersionIds,
    UUID id,
    UUID organizationId,
    String status,
    String scope,
    String evidenceIds,
    String reason,
    UUID reviewerId,
    Instant createdAt,
    Instant reviewedAt,
    Instant validUntil) {
  public static IssuerApplicationResponse from(IssuerApplication value) {
    return new IssuerApplicationResponse(
        value.getScopeMode(),
        value.getCourseVersionIds(),
        value.getId(),
        value.getOrganizationId(),
        value.getStatus(),
        value.getScope(),
        value.getEvidenceIds(),
        value.getReason(),
        value.getReviewerId(),
        value.getCreatedAt(),
        value.getReviewedAt(),
        value.getValidUntil());
  }
}
