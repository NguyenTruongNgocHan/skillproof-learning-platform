package com.skillproof.backend.learning.application;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.skillproof.backend.common.exception.NotFoundException;
import com.skillproof.backend.learning.contract.LearningQuizAccess;

@Service
public class LearningQuizAccessService implements LearningQuizAccess {

    private final LearningContextReader contextReader;
    private final LearningAccess access;
    private final LearningProgressService progress;

    public LearningQuizAccessService(
            LearningContextReader contextReader,
            LearningAccess access,
            LearningProgressService progress) {
        this.contextReader = contextReader;
        this.access = access;
        this.progress = progress;
    }

    @Override
    public void requireLearner(UUID learnerId) {
        access.learner(learnerId);
    }

    @Override
    public void requireOrganizer(UUID actorId, UUID organizationId) {
        access.organizer(actorId, organizationId);
    }

    @Override
    public VersionContext version(UUID versionId) {
        return contextReader.version(versionId)
                .orElseThrow(() -> new NotFoundException(
                "VERSION_NOT_FOUND", "Learning Path version not found"));
    }

    @Override
    public EnrollmentContext enrollment(UUID learnerId, UUID enrollmentId, boolean lock) {
        access.learner(learnerId);
        return contextReader.enrollment(learnerId, enrollmentId, lock)
                .orElseThrow(() -> new NotFoundException(
                "ENROLLMENT_NOT_FOUND", "Enrollment not found"));
    }

    @Override
    public void evaluateCompletion(UUID enrollmentId, int requiredAssessments, int passedAssessments) {
        progress.evaluate(enrollmentId, requiredAssessments, passedAssessments);
    }
}
