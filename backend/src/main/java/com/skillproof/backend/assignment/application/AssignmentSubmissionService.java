package com.skillproof.backend.assignment.application;

import java.time.Instant;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.skillproof.backend.assignment.domain.SubmissionRules;
import com.skillproof.backend.assignment.infrastructure.persistence.*;
import com.skillproof.backend.course.contract.*;
import com.skillproof.backend.media.contract.SubmissionMediaQuery;
import com.skillproof.backend.common.exception.*;

@Service
public class AssignmentSubmissionService {

    @jakarta.persistence.PersistenceContext
    private jakarta.persistence.EntityManager entityManager;
    private final AssignmentRepository assignments;
    private final AssignmentSubmissionRepository submissions;
    private final CourseStructureQuery courses;
    private final CompletionEvidenceQuery completion;
    private final SubmissionMediaQuery media;

    public AssignmentSubmissionService(AssignmentRepository assignments, AssignmentSubmissionRepository submissions, CourseStructureQuery courses,
            CompletionEvidenceQuery completion, SubmissionMediaQuery media) {
        this.assignments = assignments;
        this.submissions = submissions;
        this.courses = courses;
        this.completion = completion;
        this.media = media;
    }

    private AssignmentEntity assignment(UUID id) {
        return assignments.findById(id).orElseThrow(() -> new NotFoundException("ASSIGNMENT_NOT_FOUND", "Assignment not found"));
    }

    private AssignmentSubmissionEntity owned(UUID actor, UUID id) {
        return submissions.findById(id).filter(s -> actor.equals(s.getLearnerId())).orElseThrow(() -> new NotFoundException("SUBMISSION_NOT_FOUND", "Submission not found"));
    }

    @Transactional(readOnly = true)
    public List<AssignmentEntity> forEnrollment(UUID learner, UUID enrollment) {
        UUID version = courses.enrollmentVersion(learner, enrollment);
        courses.requireEnrollment(learner, enrollment, version, false);
        return assignments.findByVersionId(version);
    }

    @Transactional
    public AssignmentSubmissionEntity prepare(UUID learner, UUID assignmentId, UUID enrollment, String body, String link) {
        var a = assignment(assignmentId);
        courses.requireEnrollment(learner, enrollment, a.getVersionId(), true);
        if (a.getDueAt() != null && !a.getDueAt().isAfter(Instant.now())) {
            throw new ConflictException("ASSIGNMENT_CLOSED", "Assignment deadline passed");
        }
        if (body != null && body.length() > 20000 || link != null && (link.length() > 1000 || !link.matches("https://.+"))) {
            throw new BadRequestException("SUBMISSION_CONTENT", "Text or HTTPS link required");
        }
        var previous = submissions.findByAssignmentIdAndEnrollmentIdOrderByCreatedAtDesc(assignmentId, enrollment);
        if (previous.stream().anyMatch(s -> Boolean.TRUE.equals(s.getPassed()))) {
            throw new ConflictException("ASSIGNMENT_PASSED", "Assignment already passed");
        }
        var pending = previous.stream().filter(s -> !"GRADED".equals(s.getStatus())).findFirst();
        if (pending.isPresent()) {
            return pending.get();
        }
        if (previous.size() >= a.getMaxSubmissions()) {
            throw new ConflictException("SUBMISSION_LIMIT", "No submissions remaining");
        }
        return submissions.save(new AssignmentSubmissionEntity(UUID.randomUUID(), assignmentId, enrollment, learner, "DRAFT", body, link, Instant.now(), null, null, null, null, null, null));
    }

    @Transactional
    public AssignmentSubmissionEntity editDraft(UUID learner, UUID id, String body, String link) {
        var snapshot = owned(learner, id);
        var a = assignment(snapshot.getAssignmentId());
        courses.requireEnrollment(learner, snapshot.getEnrollmentId(), a.getVersionId(), true);
        var s = submissions.lock(id).orElseThrow();
        entityManager.refresh(s, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
        if (!"DRAFT".equals(s.getStatus())) {
            throw new ConflictException("SUBMISSION_IMMUTABLE", "Only a draft can be edited");
        }
        if (a.getDueAt() != null && !a.getDueAt().isAfter(Instant.now())) {
            throw new ConflictException("ASSIGNMENT_CLOSED", "Assignment deadline passed");
        }
        if (body != null && body.length() > 20000 || link != null && (link.length() > 1000 || !link.matches("https://.+"))) {
            throw new BadRequestException("SUBMISSION_CONTENT", "Text or HTTPS link required");
        }
        s.editDraft(body, link);
        return submissions.save(s);
    }

    @Transactional
    public AssignmentSubmissionEntity submit(UUID learner, UUID id) {
        var snapshot = owned(learner, id);
        var a = assignment(snapshot.getAssignmentId());
        courses.requireEnrollment(learner, snapshot.getEnrollmentId(), a.getVersionId(), true);
        var s = submissions.lock(id).orElseThrow();
        entityManager.refresh(s, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
        if (!"DRAFT".equals(s.getStatus())) {
            return s;
        }
        if (a.getDueAt() != null && !a.getDueAt().isAfter(Instant.now())) {
            throw new ConflictException("ASSIGNMENT_CLOSED", "Assignment deadline passed");
        }
        if ((s.getBody() == null || s.getBody().isBlank()) && (s.getLink() == null || s.getLink().isBlank()) && !media.hasSubmissionFiles(id)) {
            throw new BadRequestException("SUBMISSION_EMPTY", "Submit text, a link or an uploaded file");
        }
        s.submit(Instant.now());
        return submissions.save(s);
    }

    @Transactional
    public AssignmentSubmissionEntity grade(UUID actor, UUID id, int score, String feedback) {
        var snapshot = submissions.findById(id).orElseThrow(() -> new NotFoundException("SUBMISSION_NOT_FOUND", "Submission not found"));
        var a = assignment(snapshot.getAssignmentId());
        courses.requireVersionAuthor(actor, a.getVersionId());
        courses.lockEnrollment(snapshot.getEnrollmentId());
        var s = submissions.lock(id).orElseThrow();
        entityManager.refresh(s, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
        if ("GRADED".equals(s.getStatus())) {
            if (!Objects.equals(s.getScorePercent(), score) || !Objects.equals(s.getFeedback(), feedback)) {
                throw new ConflictException("GRADE_FINAL", "Grade is immutable");
            }
            return s;
        }
        if (!"SUBMITTED".equals(s.getStatus())) {
            throw new ConflictException("SUBMISSION_NOT_READY", "Submit before grading");
        }
        if (score < 0 || score > 100 || feedback == null || feedback.isBlank() || feedback.length() > 2000) {
            throw new BadRequestException("GRADE_INVALID", "Valid score and feedback required");
        }
        s.grade(score, SubmissionRules.passes(score, a.getPassPercent()), feedback, actor, Instant.now());
        submissions.saveAndFlush(s);
        completion.evaluate(s.getEnrollmentId(), 0, 0);
        return s;
    }

    @Transactional(readOnly = true)
    public List<AssignmentSubmissionEntity> authorSubmissions(UUID actor, UUID assignmentId) {
        courses.requireVersionAuthor(actor, assignment(assignmentId).getVersionId());
        return submissions.findByAssignmentIdOrderByCreatedAtDesc(assignmentId);
    }

    @Transactional(readOnly = true)
    public List<AssignmentSubmissionEntity> mine(UUID learner, UUID assignmentId, UUID enrollment) {
        courses.requireEnrollment(learner, enrollment, assignment(assignmentId).getVersionId(), false);
        return submissions.findByAssignmentIdAndEnrollmentIdOrderByCreatedAtDesc(assignmentId, enrollment);
    }
}
