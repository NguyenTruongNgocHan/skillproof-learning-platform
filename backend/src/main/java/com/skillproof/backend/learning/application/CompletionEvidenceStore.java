package com.skillproof.backend.learning.application;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface CompletionEvidenceStore {

    record Enrollment(UUID id, UUID learnerId, UUID versionId) {

    }

    record Policy(UUID id, boolean requireAllResources, boolean requireOfficialAssessments) {

    }

    Enrollment lockEnrollment(UUID enrollmentId);

    boolean belongsToLearner(UUID enrollmentId, UUID learnerId);

    long resourceCount(UUID versionId);

    long completedResourceCount(UUID enrollmentId);

    Policy policy(UUID versionId);

    void markCompleted(UUID enrollmentId, Instant now);

    void record(UUID evaluationId, UUID enrollmentId, UUID versionId,
            String status, String evidenceJson, Instant evaluatedAt);

    Optional<String> latestEvidenceJson(UUID enrollmentId);
}
