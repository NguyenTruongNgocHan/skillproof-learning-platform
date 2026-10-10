package com.skillproof.backend.assignment.application;

import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;
import com.skillproof.backend.assignment.contract.AssignmentMediaAccess;
import com.skillproof.backend.assignment.infrastructure.persistence.*;
import com.skillproof.backend.course.contract.CourseStructureQuery;
import com.skillproof.backend.common.exception.*;

@Service
public class AssignmentMediaAccessService implements AssignmentMediaAccess {

    @jakarta.persistence.PersistenceContext
    private jakarta.persistence.EntityManager entityManager;
    private final AssignmentSubmissionRepository submissions;
    private final AssignmentRepository assignments;
    private final CourseStructureQuery courses;

    public AssignmentMediaAccessService(AssignmentSubmissionRepository submissions, AssignmentRepository assignments, CourseStructureQuery courses) {
        this.submissions = submissions;
        this.assignments = assignments;
        this.courses = courses;
    }

    @Override
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.MANDATORY)
    public void requireWrite(UUID actor, UUID id) {
        var snapshot = submissions.findById(id).orElseThrow(() -> new NotFoundException("SUBMISSION_NOT_FOUND", "Submission not found"));
        var a = assignments.findById(snapshot.getAssignmentId()).orElseThrow();
        courses.requireEnrollment(actor, snapshot.getEnrollmentId(), a.getVersionId(), true);
        if (a.getDueAt() != null && !a.getDueAt().isAfter(java.time.Instant.now())) {
            throw new ConflictException("ASSIGNMENT_CLOSED", "Assignment deadline passed");
        }
        var submission = submissions.lock(id).orElseThrow();
        entityManager.refresh(submission, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
        if (!actor.equals(submission.getLearnerId()) || !"DRAFT".equals(submission.getStatus())) {
            throw new AccessDeniedException("Only the learner's draft submission is editable");
        }
    }

    @Override
    public void requireRead(UUID actor, UUID id) {
        var s = submissions.findById(id).orElseThrow(() -> new NotFoundException("SUBMISSION_NOT_FOUND", "Submission not found"));
        var a = assignments.findById(s.getAssignmentId()).orElseThrow();
        if (actor.equals(s.getLearnerId())) {
            courses.requireEnrollment(actor, s.getEnrollmentId(), a.getVersionId(), false); 
        }else {
            courses.requireVersionAuthor(actor, a.getVersionId());
        }
    }
}
