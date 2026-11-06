package com.skillproof.backend.organization.api.model;

import com.skillproof.backend.organization.infrastructure.EvidenceDocument;
import java.time.*;
import java.util.*;

/** Stable HTTP projection; persistence entities are never serialized by this endpoint. */
public record EvidenceDocumentResponse(
    UUID id,
    UUID organizationId,
    UUID mediaId,
    String kind,
    String purpose,
    String title,
    Instant expiresAt,
    String status,
    String reason,
    UUID reviewerId,
    Instant createdAt,
    Instant reviewedAt) {
  public static EvidenceDocumentResponse from(EvidenceDocument value) {
    return new EvidenceDocumentResponse(
        value.getId(),
        value.getOrganizationId(),
        value.getMediaId(),
        value.getKind(),
        value.getPurpose(),
        value.getTitle(),
        value.getExpiresAt(),
        value.getStatus(),
        value.getReason(),
        value.getReviewerId(),
        value.getCreatedAt(),
        value.getReviewedAt());
  }
}
