package com.skillproof.backend.learning.application;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillproof.backend.common.exception.NotFoundException;
import com.skillproof.backend.learning.contract.CompletionEvidenceQuery;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Service
public class CompletionEvidenceService implements CompletionEvidenceQuery {

    private final CompletionEvidenceStore evidenceStore;
    private final ObjectMapper json;

    public CompletionEvidenceService(CompletionEvidenceStore evidenceStore, ObjectMapper json) {
        this.evidenceStore = evidenceStore;
        this.json = json;
    }

    @Override
    @Transactional
    public Evidence evaluate(UUID enrollmentId, int required, int passed) {
        CompletionEvidenceStore.Enrollment enrollment = evidenceStore.lockEnrollment(enrollmentId);
        UUID versionId = enrollment.versionId();
        int total = Math.toIntExact(evidenceStore.resourceCount(versionId));
        int completed = Math.toIntExact(evidenceStore.completedResourceCount(enrollmentId));
        CompletionEvidenceStore.Policy policy = evidenceStore.policy(versionId);

        boolean resourcesMet = !policy.requireAllResources() || (total > 0 && completed == total);
        boolean assessmentsMet = !policy.requireOfficialAssessments()
                || (required > 0 && passed == required);
        boolean done = resourcesMet && assessmentsMet;
        if (done) {
            evidenceStore.markCompleted(enrollmentId, Instant.now());
        }

        UUID evaluationId = UUID.randomUUID();
        Evidence evidence = new Evidence(evaluationId, enrollmentId, enrollment.learnerId(),
                versionId, policy.id(), total, completed, required, passed, done);
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
        return evidenceStore.latestEvidenceJson(enrollmentId)
                .map(this::deserialize)
                .orElseGet(() -> evaluate(enrollmentId, 0, 0));
    }

    private Evidence deserialize(String value) {
        try {
            return json.readValue(value, Evidence.class);
        } catch (JacksonException ex) {
            throw new IllegalStateException("Cannot deserialize completion evidence", ex);
        }
    }
}
