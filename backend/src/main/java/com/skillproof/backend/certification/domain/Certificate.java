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
        String revocationReason,
        UUID organizationId,
        String programName,
        UUID courseVersionId,
        String issuerName,
        String learnerEmail
        ) {

    public Certificate(
            UUID id,
            UUID certificationProgramId,
            UUID eligibilityId,
            UUID learnerUserId,
            String serialNumber,
            Status status,
            Instant issuedAt,
            Instant revokedAt,
            String revocationReason
    ) {
        this(
                id,
                certificationProgramId,
                eligibilityId,
                learnerUserId,
                serialNumber,
                status,
                issuedAt,
                revokedAt,
                revocationReason,
                null,
                null,
                null,
                null,
                null
        );
    }

    public Certificate(
            UUID id,
            UUID programId,
            UUID eligibilityId,
            UUID learnerId,
            String serial,
            Status status,
            Instant issuedAt,
            Instant revokedAt,
            String reason,
            UUID organizationId,
            String programName,
            UUID versionId
    ) {
        this(
                id,
                programId,
                eligibilityId,
                learnerId,
                serial,
                status,
                issuedAt,
                revokedAt,
                reason,
                organizationId,
                programName,
                versionId,
                null,
                null
        );
    }

    public enum Status {
        ISSUED,
        REVOKED,
    }
}
