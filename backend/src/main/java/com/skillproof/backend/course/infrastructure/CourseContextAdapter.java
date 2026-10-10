package com.skillproof.backend.course.infrastructure;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.skillproof.backend.course.application.CourseContextReader;
import com.skillproof.backend.course.contract.CertificationContextQuery;
import com.skillproof.backend.course.contract.CourseQuizAccess;

@Component
public class CourseContextAdapter implements CourseContextReader {

    private final CourseContextRepository versions;
    private final CourseEnrollmentRepository enrollments;
    private final CourseResourceContextRepository resources;

    public CourseContextAdapter(
            CourseContextRepository versions,
            CourseEnrollmentRepository enrollments,
            CourseResourceContextRepository resources
    ) {
        this.versions = versions;
        this.enrollments = enrollments;
        this.resources = resources;
    }

    @Override
    public java.util.List<CertificationContextQuery.PublishedSource> publishedSources(
            UUID organizationId
    ) {
        return versions
                .findPublishedSources(organizationId)
                .stream()
                .map(v
                        -> new CertificationContextQuery.PublishedSource(
                        v.getId(),
                        v.getTitle(),
                        v.getVersionNo()
                )
                )
                .toList();
    }

    @Override
    public Optional<CertificationContextQuery.CertificationContext> certification(
            UUID versionId
    ) {
        return versions
                .findCertificationContext(versionId)
                .map(projection
                        -> new CertificationContextQuery.CertificationContext(
                        projection.getId(),
                        projection.getOrganizationId(),
                        projection.getCompletionPolicyId(),
                        "PUBLISHED".equals(projection.getStatus())
                )
                );
    }

    @Override
    public Optional<CourseQuizAccess.VersionContext> lockVersion(UUID versionId) {
        return versions.findForUpdate(versionId).flatMap(ignored -> version(versionId));
    }

    @Override
    public Optional<CourseQuizAccess.VersionContext> version(UUID versionId) {
        return versions
                .findVersionContext(versionId)
                .map(projection
                        -> new CourseQuizAccess.VersionContext(
                        projection.getId(),
                        projection.getOrganizationId(),
                        projection.getStatus()
                )
                );
    }

    @Override
    public Optional<CourseQuizAccess.EnrollmentContext> enrollment(
            UUID learnerId,
            UUID enrollmentId,
            boolean lock
    ) {
        Optional<EnrollmentEntity> value = lock
                ? enrollments.findForUpdate(enrollmentId, learnerId)
                : enrollments.findByIdAndLearnerId(enrollmentId, learnerId);
        return value.map(enrollment
                -> new CourseQuizAccess.EnrollmentContext(
                        enrollment.getId(),
                        enrollment.getLearnerId(),
                        enrollment.getVersionId(),
                        enrollment.getStatus()
                )
        );
    }

    @Override
    public Optional<ResourceContext> resource(UUID resourceId) {
        return resources
                .findResourceContext(resourceId)
                .map(projection
                        -> new ResourceContext(
                        projection.getOrganizationId(),
                        projection.getVersionId(),
                        projection.getStatus(),
                        projection.getKind()
                )
                );
    }

    @Override
    public boolean hasEnrollment(UUID learnerId, UUID versionId) {
        return enrollments.existsByLearnerIdAndVersionId(learnerId, versionId);
    }
}
