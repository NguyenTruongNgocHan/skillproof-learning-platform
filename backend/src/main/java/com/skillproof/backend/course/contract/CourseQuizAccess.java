package com.skillproof.backend.course.contract;

import java.util.UUID;

/**
 * Public Course contract consumed by Quiz; no Course repository/entity is
 * exposed.
 */
public interface CourseQuizAccess {

    record VersionContext(UUID versionId, UUID organizationId, String status) {

    }

    record EnrollmentContext(UUID enrollmentId, UUID learnerId, UUID versionId, String status) {

    }

    void requireLearner(UUID learnerId);

    void requirePersonalAuthor(UUID actor, UUID owner);

    void requireVersionAuthor(UUID actor, UUID version);

    UUID versionAuthor(UUID version);

    void requireOrganizer(UUID actorId, UUID organizationId);

    VersionContext version(UUID versionId);

    VersionContext lockVersion(UUID versionId);

    EnrollmentContext enrollment(UUID learnerId, UUID enrollmentId, boolean lock);

    void evaluateCompletion(UUID enrollmentId, int requiredAssessments, int passedAssessments);
}
