package com.skillproof.backend.certification.api;

import com.skillproof.backend.certification.domain.CertificationProgram;
import java.time.Instant;
import java.util.UUID;

public record CertificationProgramResponse(
        UUID id,
        UUID organizationId,
        UUID learningPathVersionId,
        UUID completionPolicyId,
        String name,
        String status,
        Instant createdAt) {

    static CertificationProgramResponse from(CertificationProgram program) {
        return new CertificationProgramResponse(
                program.id(), program.organizationId(), program.learningPathVersionId(),
                program.completionPolicyId(), program.name(), program.status().name(), program.createdAt());
    }
}
