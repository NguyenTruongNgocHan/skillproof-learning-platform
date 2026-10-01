package com.skillproof.backend.learning.contract;

import java.util.UUID;

/**
 * Narrow collaboration contract used by Learning for Quiz-owned assessment
 * state.
 */
public interface LearningAssessmentDependency {

    void clonePublishedAssessments(UUID sourceVersionId, UUID targetVersionId);

    boolean hasDraftAssessments(UUID learningPathVersionId);

    boolean hasPublishedOfficialAssessment(UUID learningPathVersionId);
}
