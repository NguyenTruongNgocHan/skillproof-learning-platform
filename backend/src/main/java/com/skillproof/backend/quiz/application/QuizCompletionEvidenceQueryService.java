package com.skillproof.backend.quiz.application;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.skillproof.backend.quiz.contract.QuizCompletionEvidenceQuery;
import com.skillproof.backend.quiz.infrastructure.persistence.QuizAssessment;
import com.skillproof.backend.quiz.infrastructure.persistence.QuizAssessmentRepository;
import com.skillproof.backend.quiz.infrastructure.persistence.QuizAttemptRepository;

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
                .filter(a -> a.getKind() == QuizAssessment.Kind.OFFICIAL && a.isRequiredForCompletion()).toList();
        var ids = official.stream().map(a -> a.getId()).toList();
        if (ids.isEmpty()) {
            return new AssessmentEvidence(0, 0);
        }
        return new AssessmentEvidence(
                official.size(),
                (int) attempts.countDistinctPassedAssessments(ids, enrollmentId)
        );
    }
}
