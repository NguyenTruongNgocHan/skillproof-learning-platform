package com.skillproof.backend.certification.domain;

import java.time.Instant;
import java.util.UUID;

public record CertificateEligibility(
        UUID id,
        UUID certificationProgramId,
        UUID learnerUserId,
        UUID enrollmentId,
        UUID completionEvaluationId,
        Status status,
        String evidenceSnapshotJson,
        Instant evaluatedAt) {

    public enum Status {
        ELIGIBLE,
        NOT_ELIGIBLE
    }
}
