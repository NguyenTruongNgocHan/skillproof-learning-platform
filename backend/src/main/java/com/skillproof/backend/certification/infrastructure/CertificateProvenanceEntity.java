package com.skillproof.backend.certification.infrastructure;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "certificate_provenance")
public class CertificateProvenanceEntity {
    @Id private UUID id;

    @Column(name = "issuer_application_id", nullable = false)
    private UUID issuerApplicationId;

    @Column(name = "credential_hash", nullable = false, length = 64)
    private String credentialHash;

    @Column(name = "canonical_payload", nullable = false, columnDefinition = "text")
    private String canonicalPayload;

    @Column(name = "captured_at", nullable = false)
    private Instant capturedAt;

    protected CertificateProvenanceEntity() {}

    public CertificateProvenanceEntity(
            UUID id, UUID issuer, String hash, String payload, Instant at) {
        this.id = id;
        issuerApplicationId = issuer;
        credentialHash = hash;
        canonicalPayload = payload;
        capturedAt = at;
    }

    public UUID getIssuerApplicationId() {
        return issuerApplicationId;
    }

    public String getCredentialHash() {
        return credentialHash;
    }

    public Instant getCapturedAt() {
        return capturedAt;
    }
}
