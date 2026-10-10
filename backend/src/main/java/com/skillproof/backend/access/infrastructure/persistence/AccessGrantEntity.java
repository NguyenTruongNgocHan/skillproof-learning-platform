package com.skillproof.backend.access.infrastructure.persistence;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "access_grant")
public class AccessGrantEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "learner_id", nullable = false)
    private UUID learnerId;

    @Column(name = "product_type", nullable = false, length = 20)
    private String productType;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "source", nullable = false, length = 20)
    private String source;

    @Column(name = "reference_id", nullable = false)
    private UUID referenceId;

    @Column(name = "granted_by", nullable = true)
    private UUID grantedBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = true)
    private Instant expiresAt;

    @Column(name = "revoked_at", nullable = true)
    private Instant revokedAt;

    protected AccessGrantEntity() {
    }

    public AccessGrantEntity(UUID id, UUID learnerId, String productType, UUID productId, String source, UUID referenceId, UUID grantedBy, Instant createdAt, Instant expiresAt, Instant revokedAt) {
        this.id = id;
        this.learnerId = learnerId;
        this.productType = productType;
        this.productId = productId;
        this.source = source;
        this.referenceId = referenceId;
        this.grantedBy = grantedBy;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.revokedAt = revokedAt;
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

    public String getSource() {
        return source;
    }

    public UUID getReferenceId() {
        return referenceId;
    }

    public UUID getGrantedBy() {
        return grantedBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getRevokedAt() {
        return revokedAt;
    }

    public void revoke(Instant now) {
        if (revokedAt == null) {
            revokedAt = now;
    
        }}

    public boolean effective(Instant now) {
        return com.skillproof.backend.access.domain.GrantPolicy.effective(expiresAt, revokedAt, now);
    }
}
