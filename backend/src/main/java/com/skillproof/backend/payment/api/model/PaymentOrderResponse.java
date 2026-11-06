package com.skillproof.backend.payment.api.model;

import com.skillproof.backend.payment.infrastructure.persistence.PaymentOrderEntity;
import java.time.Instant;
import java.util.UUID;

/** Stable HTTP projection; persistence entities are never serialized by this endpoint. */
public record PaymentOrderResponse(
    UUID organizationId,
    UUID id,
    UUID learnerId,
    String productType,
    UUID productId,
    String idempotencyKey,
    long amountVnd,
    String status,
    String providerTransaction,
    Instant createdAt,
    Instant expiresAt,
    Instant settledAt) {
  public static PaymentOrderResponse from(PaymentOrderEntity value) {
    return new PaymentOrderResponse(
        value.getOrganizationId(),
        value.getId(),
        value.getLearnerId(),
        value.getProductType(),
        value.getProductId(),
        value.getIdempotencyKey(),
        value.getAmountVnd(),
        "PENDING".equals(value.getStatus())
                && !value.getExpiresAt().isAfter(java.time.Instant.now())
            ? "EXPIRED"
            : value.getStatus(),
        value.getProviderTransaction(),
        value.getCreatedAt(),
        value.getExpiresAt(),
        value.getSettledAt());
  }
}
