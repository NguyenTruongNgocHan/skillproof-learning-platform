package com.skillproof.backend.operations.api.model;

import com.skillproof.backend.operations.infrastructure.WorkspaceNotice;
import java.time.*;
import java.util.*;

/** Stable HTTP projection; persistence entities are never serialized by this endpoint. */
public record WorkspaceNoticeResponse(
    UUID id,
    UUID recipientId,
    UUID organizationId,
    String category,
    String message,
    String link,
    Instant createdAt,
    Instant readAt) {
  public static WorkspaceNoticeResponse from(WorkspaceNotice value) {
    return new WorkspaceNoticeResponse(
        value.getId(),
        value.getRecipientId(),
        value.getOrganizationId(),
        value.getCategory(),
        value.getMessage(),
        value.getLink(),
        value.getCreatedAt(),
        value.getReadAt());
  }
}
