package com.skillproof.backend.library.api.model;

import com.skillproof.backend.library.infrastructure.persistence.LibraryResourceEntity;
import java.time.Instant;
import java.util.UUID;

/** Stable HTTP projection; persistence entities are never serialized by this endpoint. */
public record LibraryResourceResponse(
    String accessMode,
    UUID id,
    UUID authorId,
    UUID organizationId,
    String title,
    String summary,
    String body,
    String status,
    long priceVnd,
    Instant createdAt,
    UUID reviewedBy,
    String reviewReason) {
  public static LibraryResourceResponse from(LibraryResourceEntity value) {
    return new LibraryResourceResponse(
        value.getAccessMode(),
        value.getId(),
        value.getAuthorId(),
        value.getOrganizationId(),
        value.getTitle(),
        value.getSummary(),
        value.getBody(),
        value.getStatus(),
        value.getPriceVnd(),
        value.getCreatedAt(),
        value.getReviewedBy(),
        value.getReviewReason());
  }
}
