package com.skillproof.backend.quiz.application;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.skillproof.backend.quiz.contract.QuizCompletionEvidenceQuery;

@Service
public class QuizCompletionEvidenceQueryService implements QuizCompletionEvidenceQuery {

    private final QuizAssessmentRepository assessments;
    private final QuizAttemptRepository attempts;

    public QuizCompletionEvidenceQueryService(QuizAssessmentRepository assessments, QuizAttemptRepository attempts) {
        this.assessments = assessments;
        this.attempts = attempts;
    }

    public AssessmentEvidence evidence(UUID versionId, UUID enrollmentId) {
        var official = assessments.findByVersionIdAndStatus(versionId, QuizAssessment.Status.PUBLISHED).stream()
                .filter(a -> a.kind == QuizAssessment.Kind.OFFICIAL).toList();
        var ids = official.stream().map(a -> a.id).toList();
        return new AssessmentEvidence(official.size(), (int) attempts.countByAssessmentIdInAndEnrollmentIdAndPassedTrue(ids, enrollmentId));
    }
}
