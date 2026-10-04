package com.skillproof.backend.certification.infrastructure;

import com.skillproof.backend.certification.domain.Certificate;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "certificate")
class CertificateEntity {
    @Id UUID id;
    @Column(name = "certification_program_id", nullable = false) UUID certificationProgramId;
    @Column(name = "eligibility_id", nullable = false) UUID eligibilityId;
    @Column(name = "learner_user_id", nullable = false) UUID learnerUserId;
    @Column(name = "serial_number", nullable = false, unique = true) String serialNumber;
    @Enumerated(EnumType.STRING) @Column(nullable = false) Certificate.Status status;
    @Column(name = "issued_at", nullable = false) Instant issuedAt;
    @Column(name = "revoked_at") Instant revokedAt;
    @Column(name = "revocation_reason") String revocationReason;

    protected CertificateEntity() {}

    CertificateEntity(Certificate c) {
        id = c.id(); certificationProgramId = c.certificationProgramId();
        eligibilityId = c.eligibilityId(); learnerUserId = c.learnerUserId();
        serialNumber = c.serialNumber(); status = c.status(); issuedAt = c.issuedAt();
        revokedAt = c.revokedAt(); revocationReason = c.revocationReason();
    }

    Certificate domain() {
        return new Certificate(id, certificationProgramId, eligibilityId, learnerUserId,
                serialNumber, status, issuedAt, revokedAt, revocationReason);
    }

    void revoke(Instant at, String reason) {
        status = Certificate.Status.REVOKED;
        revokedAt = at;
        revocationReason = reason;
    }
}
