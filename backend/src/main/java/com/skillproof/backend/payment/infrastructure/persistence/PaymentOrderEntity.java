package com.skillproof.backend.payment.infrastructure.persistence;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "payment_order")
public class PaymentOrderEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "learner_id", nullable = false)
    private UUID learnerId;

    @Column(name = "product_type", nullable = false, length = 20)
    private String productType;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "idempotency_key", nullable = false, length = 100)
    private String idempotencyKey;

    @Column(name = "amount_vnd", nullable = false)
    private long amountVnd;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "provider_transaction", nullable = true, length = 32)
    private String providerTransaction;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "settled_at", nullable = true)
    private Instant settledAt;

    protected PaymentOrderEntity() {
    }

    public PaymentOrderEntity(UUID id, UUID learnerId, String productType, UUID productId, String idempotencyKey, long amountVnd, String status, String providerTransaction, Instant createdAt, Instant expiresAt, Instant settledAt) {
        this.id = id;
        this.learnerId = learnerId;
        this.productType = productType;
        this.productId = productId;
        this.idempotencyKey = idempotencyKey;
        this.amountVnd = amountVnd;
        this.status = status;
        this.providerTransaction = providerTransaction;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.settledAt = settledAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getLearnerId() {
        return learnerId;
    }

    public String getProductType() {
        return productType;
    }

    public UUID getProductId() {
        return productId;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public long getAmountVnd() {
        return amountVnd;
    }

    public String getStatus() {
        return status;
    }

    public String getProviderTransaction() {
        return providerTransaction;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getSettledAt() {
        return settledAt;
    }

    public void settle(com.skillproof.backend.payment.domain.PaymentStatus status, String transaction, Instant now) {
        if (!"PENDING".equals(this.status)) {
            throw new IllegalStateException("Payment already finalized");
        }
        this.status = status.name();
        providerTransaction = transaction;
        settledAt = now;
    }
}
