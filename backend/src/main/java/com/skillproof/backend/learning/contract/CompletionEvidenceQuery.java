package com.skillproof.backend.learning.contract;

import java.util.UUID;

/**
 * Stable Learning-owned completion projection consumed by Certification.
 */
public interface CompletionEvidenceQuery {

    record Evidence(
            UUID evaluationId,
            UUID enrollmentId,
            UUID learnerId,
            UUID pathVersionId,
            UUID completionPolicyId,
            int totalResources,
            int completedResources,
            int requiredAssessments,
            int passedAssessments,
            boolean completed) {

    }

    Evidence evaluate(UUID enrollmentId, int requiredAssessments, int passedAssessments);

    Evidence readForLearner(UUID enrollmentId, UUID learnerId);
}
