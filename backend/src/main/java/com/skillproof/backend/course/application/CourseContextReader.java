package com.skillproof.backend.course.application;

import java.util.Optional;
import java.util.UUID;

import com.skillproof.backend.course.contract.CertificationContextQuery;
import com.skillproof.backend.course.contract.CourseQuizAccess;

public interface CourseContextReader {

    record ResourceContext(
            UUID organizationId,
            UUID versionId,
            String status,
            String kind
            ) {

    }

    java.util.List<CertificationContextQuery.PublishedSource> publishedSources(
            UUID organizationId
    );

    Optional<CertificationContextQuery.CertificationContext> certification(
            UUID versionId
    );

    Optional<CourseQuizAccess.VersionContext> version(UUID versionId);

    Optional<CourseQuizAccess.VersionContext> lockVersion(UUID versionId);

    Optional<CourseQuizAccess.EnrollmentContext> enrollment(
            UUID learnerId,
            UUID enrollmentId,
            boolean lock
    );

    Optional<ResourceContext> resource(UUID resourceId);

    boolean hasEnrollment(UUID learnerId, UUID versionId);
}
