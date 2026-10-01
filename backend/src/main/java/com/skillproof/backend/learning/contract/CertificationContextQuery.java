package com.skillproof.backend.learning.contract;

import java.util.UUID;

/**
 * Learning-owned read contract for Certification. Consumers receive only the
 * provenance needed to bind a certification program.
 */
public interface CertificationContextQuery {

    record CertificationContext(
            UUID learningPathVersionId,
            UUID organizationId,
            UUID completionPolicyId,
            boolean published) {

    }

    CertificationContext requireCertificationContext(UUID learningPathVersionId);
}
