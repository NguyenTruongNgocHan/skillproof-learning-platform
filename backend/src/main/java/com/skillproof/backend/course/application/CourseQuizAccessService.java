package com.skillproof.backend.course.application;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.skillproof.backend.common.exception.NotFoundException;
import com.skillproof.backend.course.contract.CourseQuizAccess;

@Service
public class CourseQuizAccessService implements CourseQuizAccess {

    private final com.skillproof.backend.course.infrastructure.CourseContextRepository versions;
    private final com.skillproof.backend.course.contract.CourseStructureQuery structure;
    private final CourseContextReader contextReader;
    private final CourseAccess access;
    private final CourseProgressService progress;

    public CourseQuizAccessService(
            CourseContextReader contextReader,
            CourseAccess access,
            CourseProgressService progress, com.skillproof.backend.course.infrastructure.CourseContextRepository versions, com.skillproof.backend.course.contract.CourseStructureQuery structure) {
        this.contextReader = contextReader;
        this.access = access;
        this.progress = progress;
        this.versions = versions;
        this.structure = structure;
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
    @org.springframework.transaction.annotation.Transactional(propagation = org.springframework.transaction.annotation.Propagation.MANDATORY)
    public VersionContext lockVersion(UUID versionId) {
        return contextReader.lockVersion(versionId).orElseThrow(()
                -> new NotFoundException("VERSION_NOT_FOUND", "Course version not found"));
    }

    @Override
    public VersionContext version(UUID versionId) {
        return contextReader.version(versionId)
                .orElseThrow(() -> new NotFoundException(
                "VERSION_NOT_FOUND", "Course version not found"));
    }

    @Override
    public EnrollmentContext enrollment(UUID learnerId, UUID enrollmentId, boolean lock) {
        access.learner(learnerId);
        var enrollment = contextReader.enrollment(learnerId, enrollmentId, lock)
                .orElseThrow(() -> new NotFoundException(
                "ENROLLMENT_NOT_FOUND", "Enrollment not found"));
        structure.requireEnrollment(learnerId, enrollmentId, enrollment.versionId(), false);
        return enrollment;
    }

    @Override
    public void evaluateCompletion(UUID enrollmentId, int requiredAssessments, int passedAssessments) {
        progress.evaluate(enrollmentId, requiredAssessments, passedAssessments);
    }

    @Override
    public void requirePersonalAuthor(UUID actor, UUID owner) {
        access.learner(actor);
        if (!actor.equals(owner)) {
            throw new org.springframework.security.access.AccessDeniedException("Personal author required");
        }
    }

    @Override
    public void requireVersionAuthor(UUID actor, UUID version) {
        structure.requireVersionAuthor(actor, version);
    }

    @Override
    public UUID versionAuthor(UUID version) {
        return versions.findCourseByVersionId(version).orElseThrow().getCreatedBy();
    }

}
