package com.skillproof.backend.certification.domain;

import java.time.Instant;
import java.util.UUID;

public record CertificationProgram(
        UUID id,
        UUID organizationId,
        UUID courseVersionId,
        UUID completionPolicyId,
        String name,
        Status status,
        UUID createdByUserId,
        Instant createdAt) {

    public enum Status {
        DRAFT,
        ACTIVE,
        RETIRED
    }
}
