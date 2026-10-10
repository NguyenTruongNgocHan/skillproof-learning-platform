package com.skillproof.backend.course.application;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillproof.backend.common.exception.NotFoundException;
import com.skillproof.backend.course.contract.CompletionEvidenceQuery;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Service
public class CompletionEvidenceService implements CompletionEvidenceQuery {

    private final com.skillproof.backend.assignment.contract.AssignmentEvidenceQuery assignments;
    private final com.skillproof.backend.course.contract.LessonEvidenceQuery lessons;
    private final CompletionEvidenceStore evidenceStore;
    private final ObjectMapper json;
    private final com.skillproof.backend.quiz.contract.QuizCompletionEvidenceQuery assessments;

    public CompletionEvidenceService(CompletionEvidenceStore evidenceStore, ObjectMapper json,
            com.skillproof.backend.quiz.contract.QuizCompletionEvidenceQuery assessments, com.skillproof.backend.assignment.contract.AssignmentEvidenceQuery assignments, com.skillproof.backend.course.contract.LessonEvidenceQuery lessons) {
        this.evidenceStore = evidenceStore;
        this.json = json;
        this.assessments = assessments;
        this.assignments = assignments;
        this.lessons = lessons;
    }

    @Override
    @Transactional
    public Evidence evaluate(UUID enrollmentId, int required, int passed) {
        CompletionEvidenceStore.Enrollment enrollment = evidenceStore.lockEnrollment(enrollmentId);
        UUID versionId = enrollment.versionId();
        var assessmentEvidence = assessments.evidence(versionId, enrollmentId);
        required = assessmentEvidence.requiredOfficialAssessments();
        passed = assessmentEvidence.passedOfficialAssessments();
        int total = Math.toIntExact(evidenceStore.resourceCount(versionId));
        int completed = Math.toIntExact(evidenceStore.completedResourceCount(enrollmentId));
        CompletionEvidenceStore.Policy policy = evidenceStore.policy(versionId);

        var assignmentEvidence = assignments.evidence(versionId, enrollmentId);
        var lessonEvidence = lessons.evidence(versionId, enrollmentId);
        boolean done = com.skillproof.backend.course.domain.CompletionRules.completed(
                policy.requireAllResources(), policy.requireOfficialAssessments(), total, completed, required, passed,
                assignmentEvidence.required(), assignmentEvidence.passed(), lessonEvidence.required(), lessonEvidence.completed());
        if (done) {
            evidenceStore.markCompleted(enrollmentId, Instant.now());
        }

        UUID evaluationId = UUID.randomUUID();
        Evidence evidence = new Evidence(evaluationId, enrollmentId, enrollment.learnerId(),
                versionId, policy.id(), total, completed, required, passed, assignmentEvidence.required(), assignmentEvidence.passed(), lessonEvidence.required(), lessonEvidence.completed(), done);
        try {
            evidenceStore.record(evaluationId, enrollmentId, versionId,
                    done ? "COMPLETED" : "NOT_COMPLETED", json.writeValueAsString(evidence), Instant.now());
        } catch (JacksonException ex) {
            throw new IllegalStateException("Cannot serialize completion evidence", ex);
        }
        return evidence;
    }

    @Override
    @Transactional
    public Evidence readForLearner(UUID enrollmentId, UUID learnerId) {
        if (!evidenceStore.belongsToLearner(enrollmentId, learnerId)) {
            throw new NotFoundException("ENROLLMENT_NOT_FOUND", "Enrollment not found");
        }
        // Re-evaluate under the enrollment lock; historical snapshots are audit evidence.
        return evaluate(enrollmentId, 0, 0);
    }

}
