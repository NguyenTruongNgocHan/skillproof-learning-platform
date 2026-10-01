package com.skillproof.backend.quiz.application;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillproof.backend.learning.contract.LearningAssessmentDependency;

@Service
public class LearningAssessmentDependencyService implements LearningAssessmentDependency {

    private final QuizAssessmentRepository assessments;
    private final QuizAssessmentQuestionRepository questions;

    public LearningAssessmentDependencyService(QuizAssessmentRepository a, QuizAssessmentQuestionRepository q) {
        assessments = a;
        questions = q;
    }

    @Transactional
    public void clonePublishedAssessments(UUID source, UUID target) {
        for (var old : assessments.findByVersionIdAndStatus(source, QuizAssessment.Status.PUBLISHED)) {
            var copy = new QuizAssessment();
            copy.id = UUID.randomUUID();
            copy.organizationId = old.organizationId;
            copy.versionId = target;
            copy.kind = old.kind;
            copy.status = QuizAssessment.Status.DRAFT;
            copy.title = old.title;
            copy.durationSeconds = old.durationSeconds;
            copy.passPercent = old.passPercent;
            copy.maxAttempts = old.maxAttempts;
            copy.createdAt = Instant.now();
            assessments.save(copy);
            for (var oldQuestion : questions.findByAssessmentIdOrderByPosition(old.id)) {
                var link = new QuizAssessmentQuestion();
                link.id = new QuizAssessmentQuestionId(copy.id, oldQuestion.questionVersion.id);
                link.assessment = copy;
                link.questionVersion = oldQuestion.questionVersion;
                link.position = oldQuestion.position;
                link.points = oldQuestion.points;
                questions.save(link);
            }
        }
    }

    public boolean hasDraftAssessments(UUID version) {
        return assessments.existsByVersionIdAndStatus(version, QuizAssessment.Status.DRAFT);
    }

    public boolean hasPublishedOfficialAssessment(UUID version) {
        return assessments.existsByVersionIdAndKindAndStatus(version, QuizAssessment.Kind.OFFICIAL, QuizAssessment.Status.PUBLISHED);
    }
}
