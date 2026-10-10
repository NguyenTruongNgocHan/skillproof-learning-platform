package com.skillproof.backend.course.infrastructure;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.skillproof.backend.common.exception.NotFoundException;
import com.skillproof.backend.course.application.CompletionEvidenceStore;

import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Repository
public class JpaCompletionEvidenceStore implements CompletionEvidenceStore {

    private final CourseEnrollmentRepository enrollments;
    private final CourseResourceContextRepository resources;
    private final ResourceProgressJpaRepository progress;
    private final CompletionPolicyJpaRepository policies;
    private final CompletionEvaluationJpaRepository evaluations;
    private final ObjectMapper json;

    public JpaCompletionEvidenceStore(
            CourseEnrollmentRepository enrollments,
            CourseResourceContextRepository resources,
            ResourceProgressJpaRepository progress,
            CompletionPolicyJpaRepository policies,
            CompletionEvaluationJpaRepository evaluations,
            ObjectMapper json
    ) {
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
                .orElseThrow(() -> new NotFoundException(
                        "ENROLLMENT_NOT_FOUND",
                        "Enrollment not found"
                ));

        return new Enrollment(
                enrollment.getId(),
                enrollment.getLearnerId(),
                enrollment.getVersionId()
        );
    }

    @Override
    public boolean belongsToLearner(UUID enrollmentId, UUID learnerId) {
        return enrollments.findByIdAndLearnerId(enrollmentId, learnerId)
                .isPresent();
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
                .orElseThrow(() -> new NotFoundException(
                        "COURSE_NOT_FOUND",
                        "Completion policy not found"
                ));

        return new Policy(
                value.getLogicalId(),
                value.isRequireAllResources(),
                value.isRequireOfficialAssessments()
        );
    }

    @Override
    public void markCompleted(UUID enrollmentId, Instant now) {
        enrollments.findForCompletion(enrollmentId)
                .orElseThrow(() -> new NotFoundException(
                        "ENROLLMENT_NOT_FOUND",
                        "Enrollment not found"
                ))
                .complete(now);
    }

    @Override
    public void record(
            UUID evaluationId,
            UUID enrollmentId,
            UUID versionId,
            String status,
            String evidenceJson,
            Instant evaluatedAt
    ) {
        Map<String, Object> evidence;

        try {
            evidence = json.readValue(
                    evidenceJson,
                    new TypeReference<Map<String, Object>>() {}
            );
        } catch (JacksonException ex) {
            throw new IllegalArgumentException(
                    "Invalid completion evidence JSON",
                    ex
            );
        }

        evaluations.save(new CompletionEvaluationEntity(
                evaluationId,
                enrollmentId,
                versionId,
                status,
                evidence,
                evaluatedAt
        ));
    }

    @Override
    public Optional<String> latestEvidenceJson(UUID enrollmentId) {
        return evaluations.findFirstByEnrollmentIdOrderByEvaluatedAtDesc(enrollmentId)
                .map(evaluation -> json.writeValueAsString(
                        evaluation.getEvidenceSnapshotJson()
                ));
    }
}