package com.skillproof.backend.course.contract;

import java.util.UUID;

/**
 * Course-owned read contract for Certification. Consumers receive only the
 * provenance needed to bind a certification program.
 */
public interface CertificationContextQuery {

    record CertificationContext(
            UUID courseVersionId,
            UUID organizationId,
            UUID completionPolicyId,
            boolean published
            ) {

    }

    record PublishedSource(UUID id, String title, int versionNo) {

    }

    java.util.List<PublishedSource> publishedSources(UUID organizationId);

    CertificationContext requireCertificationContext(
            UUID courseVersionId
    );
}
