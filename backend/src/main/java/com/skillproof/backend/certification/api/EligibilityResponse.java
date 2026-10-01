package com.skillproof.backend.certification.api;

import com.skillproof.backend.certification.domain.CertificateEligibility;
import java.time.Instant;
import java.util.UUID;

public record EligibilityResponse(
        UUID id,
        UUID certificationProgramId,
        UUID learnerUserId,
        UUID enrollmentId,
        String status,
        String evidenceSnapshotJson,
        Instant evaluatedAt) {

    static EligibilityResponse from(CertificateEligibility eligibility) {
        return new EligibilityResponse(
                eligibility.id(), eligibility.certificationProgramId(), eligibility.learnerUserId(),
                eligibility.enrollmentId(), eligibility.status().name(),
                eligibility.evidenceSnapshotJson(), eligibility.evaluatedAt());
    }
}
