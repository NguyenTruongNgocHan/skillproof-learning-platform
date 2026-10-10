package com.skillproof.backend.quiz.contract;

import java.util.UUID;

/**
 * Quiz-owned completion evidence projection for an exact path
 * version/enrollment.
 */
public interface QuizCompletionEvidenceQuery {

    record AssessmentEvidence(int requiredOfficialAssessments, int passedOfficialAssessments) {

    }

    AssessmentEvidence evidence(UUID courseVersionId, UUID enrollmentId);
}
