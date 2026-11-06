package com.skillproof.backend.operations.api.model;

import com.skillproof.backend.operations.infrastructure.AuditEntry;
import java.time.*;
import java.util.*;

/** Stable HTTP projection; persistence entities are never serialized by this endpoint. */
public record AuditEntryResponse(
    UUID id,
    UUID actorId,
    UUID organizationId,
    String action,
    UUID targetId,
    String details,
    Instant createdAt) {
  public static AuditEntryResponse from(AuditEntry value) {
    return new AuditEntryResponse(
        value.getId(),
        value.getActorId(),
        value.getOrganizationId(),
        value.getAction(),
        value.getTargetId(),
        value.getDetails(),
        value.getCreatedAt());
  }
}
