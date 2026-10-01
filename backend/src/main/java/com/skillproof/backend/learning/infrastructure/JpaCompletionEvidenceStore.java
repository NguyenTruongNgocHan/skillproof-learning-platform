package com.skillproof.backend.learning.infrastructure;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import com.skillproof.backend.common.exception.NotFoundException;
import com.skillproof.backend.learning.application.CompletionEvidenceStore;

@Repository
public class JpaCompletionEvidenceStore implements CompletionEvidenceStore {

    private final LearningEnrollmentRepository enrollments;
    private final LearningResourceContextRepository resources;
    private final ResourceProgressJpaRepository progress;
    private final CompletionPolicyJpaRepository policies;
    private final CompletionEvaluationJpaRepository evaluations;
    private final ObjectMapper json;

    public JpaCompletionEvidenceStore(LearningEnrollmentRepository enrollments,
            LearningResourceContextRepository resources,
            ResourceProgressJpaRepository progress,
            CompletionPolicyJpaRepository policies,
            CompletionEvaluationJpaRepository evaluations, ObjectMapper json) {
        this.enrollments = enrollments;
        this.resources = resources;
        this.progress = progress;
        this.policies = policies;
        this.evaluations = evaluations;
        this.json = json;
    }

    @Override
    public Enrollment lockEnrollment(UUID enrollmentId) {
        EnrollmentEntity enrollment = enrollments.findForCompletion(enrollmentId)
                .orElseThrow(() -> new NotFoundException("ENROLLMENT_NOT_FOUND", "Enrollment not found"));
        return new Enrollment(enrollment.getId(), enrollment.getLearnerId(), enrollment.getVersionId());
    }

    @Override
    public boolean belongsToLearner(UUID enrollmentId, UUID learnerId) {
        return enrollments.findByIdAndLearnerId(enrollmentId, learnerId).isPresent();
    }

    @Override
    public long resourceCount(UUID versionId) {
        return resources.countByVersionId(versionId);
    }

    @Override
    public long completedResourceCount(UUID enrollmentId) {
        return progress.countByEnrollmentId(enrollmentId);
    }

    @Override
    public Policy policy(UUID versionId) {
        CompletionPolicyEntity value = policies.findById(versionId)
                .orElseThrow(() -> new NotFoundException("LEARNING_NOT_FOUND", "Completion policy not found"));
        return new Policy(value.getLogicalId(), value.isRequireAllResources(),
                value.isRequireOfficialAssessments());
    }

    @Override
    public void markCompleted(UUID enrollmentId, Instant now) {
        enrollments.findForCompletion(enrollmentId)
                .orElseThrow(() -> new NotFoundException("ENROLLMENT_NOT_FOUND", "Enrollment not found"))
                .complete(now);
    }

    @Override
    public void record(UUID evaluationId, UUID enrollmentId, UUID versionId,
            String status, String evidenceJson, Instant evaluatedAt) {
        try {
            evaluations.save(new CompletionEvaluationEntity(evaluationId, enrollmentId,
                    versionId, status, json.readTree(evidenceJson), evaluatedAt));
        } catch (JacksonException ex) {
            throw new IllegalArgumentException("Invalid completion evidence JSON", ex);
        }
    }

    @Override
    public Optional<String> latestEvidenceJson(UUID enrollmentId) {
        return evaluations.findFirstByEnrollmentIdOrderByEvaluatedAtDesc(enrollmentId)
                .map(evaluation -> evaluation.getEvidenceSnapshotJson().toString());
    }
}
