package com.skillproof.backend.access.api.model;

import com.skillproof.backend.access.infrastructure.persistence.AccessGrantEntity;
import java.time.Instant;
import java.util.UUID;

/** Stable HTTP projection; persistence entities are never serialized by this endpoint. */
public record AccessGrantResponse(
    String cohort,
    UUID id,
    UUID learnerId,
    String productType,
    UUID productId,
    String source,
    UUID referenceId,
    UUID grantedBy,
    Instant createdAt,
    Instant expiresAt,
    Instant revokedAt,
    UUID organizationId) {
  public static AccessGrantResponse from(AccessGrantEntity value) {
    return new AccessGrantResponse(
        value.getCohort(),
        value.getId(),
        value.getLearnerId(),
        value.getProductType(),
        value.getProductId(),
        value.getSource(),
        value.getReferenceId(),
        value.getGrantedBy(),
        value.getCreatedAt(),
        value.getExpiresAt(),
        value.getRevokedAt(),
        value.getOrganizationId());
  }
}
