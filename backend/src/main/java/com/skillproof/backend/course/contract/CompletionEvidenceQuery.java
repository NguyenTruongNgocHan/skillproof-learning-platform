package com.skillproof.backend.course.contract;

import java.util.UUID;

/**
 * Stable Course-owned completion projection consumed by Certification.
 */
public interface CompletionEvidenceQuery {

    record Evidence(
            UUID evaluationId,
            UUID enrollmentId,
            UUID learnerId,
            UUID courseVersionId,
            UUID completionPolicyId,
            int totalResources,
            int completedResources,
            int requiredAssessments,
            int passedAssessments,
            int requiredAssignments,
            int passedAssignments,
            int requiredLessons,
            int completedLessons,
            boolean completed) {

    }

    Evidence evaluate(UUID enrollmentId, int requiredAssessments, int passedAssessments);

    Evidence readForLearner(UUID enrollmentId, UUID learnerId);
}
