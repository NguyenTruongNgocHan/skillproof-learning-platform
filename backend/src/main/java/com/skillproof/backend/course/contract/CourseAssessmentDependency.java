package com.skillproof.backend.course.contract;

import java.util.UUID;

/**
 * Narrow collaboration contract used by Course for Quiz-owned assessment state.
 */
public interface CourseAssessmentDependency {

    default void clonePublishedAssessments(UUID sourceVersionId, UUID targetVersionId) {
        clonePublishedAssessments(sourceVersionId, targetVersionId, java.util.Map.of(), java.util.Map.of());
    }

    void clonePublishedAssessments(UUID sourceVersionId, UUID targetVersionId, java.util.Map<UUID, UUID> modules, java.util.Map<UUID, UUID> lessons);

    boolean hasOwner(String scope, UUID ownerId);

    boolean hasDraftAssessments(UUID courseVersionId);

    boolean hasPublishedOfficialAssessment(UUID courseVersionId);
}
