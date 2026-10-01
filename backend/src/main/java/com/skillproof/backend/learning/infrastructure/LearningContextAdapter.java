package com.skillproof.backend.learning.infrastructure;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.skillproof.backend.learning.application.LearningContextReader;
import com.skillproof.backend.learning.contract.CertificationContextQuery;
import com.skillproof.backend.learning.contract.LearningQuizAccess;

@Component
public class LearningContextAdapter implements LearningContextReader {

    private final LearningContextRepository versions;
    private final LearningEnrollmentRepository enrollments;
    private final LearningResourceContextRepository resources;

    public LearningContextAdapter(LearningContextRepository versions,
            LearningEnrollmentRepository enrollments, LearningResourceContextRepository resources) {
        this.versions = versions;
        this.enrollments = enrollments;
        this.resources = resources;
    }

    @Override
    public Optional<CertificationContextQuery.CertificationContext> certification(UUID versionId) {
        return versions.findCertificationContext(versionId).map(projection
                -> new CertificationContextQuery.CertificationContext(projection.getId(),
                        projection.getOrganizationId(), projection.getCompletionPolicyId(),
                        "PUBLISHED".equals(projection.getStatus())));
    }

    @Override
    public Optional<LearningQuizAccess.VersionContext> version(UUID versionId) {
        return versions.findVersionContext(versionId).map(projection
                -> new LearningQuizAccess.VersionContext(projection.getId(),
                        projection.getOrganizationId(), projection.getStatus()));
    }

    @Override
    public Optional<LearningQuizAccess.EnrollmentContext> enrollment(
            UUID learnerId, UUID enrollmentId, boolean lock) {
        Optional<EnrollmentEntity> value = lock
                ? enrollments.findForUpdate(enrollmentId, learnerId)
                : enrollments.findByIdAndLearnerId(enrollmentId, learnerId);
        return value.map(enrollment -> new LearningQuizAccess.EnrollmentContext(
                enrollment.getId(), enrollment.getLearnerId(),
                enrollment.getVersionId(), enrollment.getStatus()));
    }

    @Override
    public Optional<ResourceContext> resource(UUID resourceId) {
        return resources.findResourceContext(resourceId).map(projection
                -> new ResourceContext(projection.getOrganizationId(), projection.getVersionId(),
                        projection.getStatus(), projection.getKind()));
    }

    @Override
    public boolean hasEnrollment(UUID learnerId, UUID versionId) {
        return enrollments.existsByLearnerIdAndVersionId(learnerId, versionId);
    }
}
