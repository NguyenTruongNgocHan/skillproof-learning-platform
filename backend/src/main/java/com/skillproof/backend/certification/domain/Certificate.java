package com.skillproof.backend.certification.domain;

import java.time.Instant;
import java.util.UUID;

public record Certificate(
        UUID id,
        UUID certificationProgramId,
        UUID eligibilityId,
        UUID learnerUserId,
        String serialNumber,
        Status status,
        Instant issuedAt,
        Instant revokedAt,
        String revocationReason) {

    public enum Status {
        ISSUED, REVOKED
    }
}
