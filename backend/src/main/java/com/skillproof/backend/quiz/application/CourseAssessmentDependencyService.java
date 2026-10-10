package com.skillproof.backend.quiz.application;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillproof.backend.course.contract.CourseAssessmentDependency;
import com.skillproof.backend.quiz.infrastructure.persistence.QuizAssessment;
import com.skillproof.backend.quiz.infrastructure.persistence.QuizAssessmentQuestion;
import com.skillproof.backend.quiz.infrastructure.persistence.QuizAssessmentQuestionId;
import com.skillproof.backend.quiz.infrastructure.persistence.QuizAssessmentQuestionRepository;
import com.skillproof.backend.quiz.infrastructure.persistence.QuizAssessmentRepository;

@Service
public class CourseAssessmentDependencyService implements CourseAssessmentDependency {

    private final QuizAssessmentRepository assessments;
    private final QuizAssessmentQuestionRepository questions;

    public CourseAssessmentDependencyService(QuizAssessmentRepository a, QuizAssessmentQuestionRepository q) {
        assessments = a;
        questions = q;
    }

    @Transactional
    public void clonePublishedAssessments(UUID source, UUID target, java.util.Map<UUID, UUID> modules, java.util.Map<UUID, UUID> lessons) {
        for (var old : assessments.findByVersionIdAndStatus(source, QuizAssessment.Status.PUBLISHED)) {
            var copy = new QuizAssessment();
            copy.setId(UUID.randomUUID());
            copy.setOrganizationId(old.getOrganizationId());
            copy.setVersionId(target);
            copy.setOwnerScope(old.getOwnerScope());
            copy.setOwnerId(switch (old.getOwnerScope()) {
                case "COURSE" ->
                    target;
                case "MODULE" ->
                    modules.get(old.getOwnerId());
                case "LESSON" ->
                    lessons.get(old.getOwnerId());
                default ->
                    throw new IllegalStateException("Invalid owner");
            });
            copy.setRequiredForCompletion(old.isRequiredForCompletion());
            copy.setKind(old.getKind());
            copy.setStatus(QuizAssessment.Status.DRAFT);
            copy.setTitle(old.getTitle());
            copy.setDurationSeconds(old.getDurationSeconds());
            copy.setPassPercent(old.getPassPercent());
            copy.setMaxAttempts(old.getMaxAttempts());
            copy.setCreatedAt(Instant.now());
            assessments.save(copy);
            for (var oldQuestion : questions.findByAssessmentIdOrderByPosition(old.getId())) {
                var link = new QuizAssessmentQuestion();
                link.setId(new QuizAssessmentQuestionId(copy.getId(), oldQuestion.getQuestionVersion().getId()));
                link.setAssessment(copy);
                link.setQuestionVersion(oldQuestion.getQuestionVersion());
                link.setPosition(oldQuestion.getPosition());
                link.setPoints(oldQuestion.getPoints());
                questions.save(link);
            }
        }
    }

    @Override
    public boolean hasOwner(String scope, UUID id) {
        return assessments.existsByOwnerScopeAndOwnerId(scope, id);
    }

    public boolean hasDraftAssessments(UUID version) {
        return assessments.existsByVersionIdAndStatus(version, QuizAssessment.Status.DRAFT);
    }

    public boolean hasPublishedOfficialAssessment(UUID version) {
        return assessments.existsByVersionIdAndKindAndStatus(version, QuizAssessment.Kind.OFFICIAL, QuizAssessment.Status.PUBLISHED);
    }
}
