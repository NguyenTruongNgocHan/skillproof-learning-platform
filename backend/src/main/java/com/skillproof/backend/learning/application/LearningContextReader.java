package com.skillproof.backend.learning.application;

import com.skillproof.backend.learning.contract.CertificationContextQuery;
import com.skillproof.backend.learning.contract.LearningQuizAccess;
import java.util.Optional;
import java.util.UUID;

public interface LearningContextReader {
    record ResourceContext(
        UUID organizationId,
        UUID versionId,
        String status,
        String kind
    ) {}

    java.util.List<CertificationContextQuery.PublishedSource> publishedSources(
        UUID organizationId
    );

    Optional<CertificationContextQuery.CertificationContext> certification(
        UUID versionId
    );

    Optional<LearningQuizAccess.VersionContext> version(UUID versionId);

    Optional<LearningQuizAccess.EnrollmentContext> enrollment(
        UUID learnerId,
        UUID enrollmentId,
        boolean lock
    );

    Optional<ResourceContext> resource(UUID resourceId);

    boolean hasEnrollment(UUID learnerId, UUID versionId);
}
