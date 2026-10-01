package com.skillproof.backend.learning.contract;

import java.util.UUID;

/**
 * Public Learning contract consumed by Quiz; no Learning repository/entity is
 * exposed.
 */
public interface LearningQuizAccess {

    record VersionContext(UUID versionId, UUID organizationId, String status) {

    }

    record EnrollmentContext(UUID enrollmentId, UUID learnerId, UUID versionId, String status) {

    }

    void requireLearner(UUID learnerId);

    void requireOrganizer(UUID actorId, UUID organizationId);

    VersionContext version(UUID versionId);

    EnrollmentContext enrollment(UUID learnerId, UUID enrollmentId, boolean lock);

    void evaluateCompletion(UUID enrollmentId, int requiredAssessments, int passedAssessments);
}
