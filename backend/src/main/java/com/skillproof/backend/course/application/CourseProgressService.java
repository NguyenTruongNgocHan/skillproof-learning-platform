package com.skillproof.backend.course.application;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillproof.backend.common.exception.NotFoundException;
import com.skillproof.backend.course.infrastructure.CourseEnrollmentRepository;
import com.skillproof.backend.course.infrastructure.CourseResourceContextRepository;
import com.skillproof.backend.course.infrastructure.EnrollmentEntity;
import com.skillproof.backend.course.infrastructure.ResourceProgressEntity;
import com.skillproof.backend.course.infrastructure.ResourceProgressJpaRepository;

@Service
public class CourseProgressService {

    private final CourseEnrollmentRepository enrollments;
    private final CourseResourceContextRepository resources;
    private final ResourceProgressJpaRepository progress;
    private final CourseAccess access;
    private final CompletionEvidenceService evidence;
    private final com.skillproof.backend.quiz.contract.QuizCompletionEvidenceQuery assessments;

    public CourseProgressService(CourseEnrollmentRepository e, CourseResourceContextRepository r,
            ResourceProgressJpaRepository p, CourseAccess a, CompletionEvidenceService c,
            com.skillproof.backend.quiz.contract.QuizCompletionEvidenceQuery assessments,
            CourseEnrollmentService enrollmentService) {
        enrollments = e;
        resources = r;
        progress = p;
        access = a;
        evidence = c;
        this.assessments = assessments;
        this.enrollmentService = enrollmentService;
    }

    private final CourseEnrollmentService enrollmentService;

    private EnrollmentEntity enrollmentEntity(UUID l, UUID id) {
        access.learner(l);
        return enrollments.findByIdAndLearnerId(id, l).orElseThrow(() -> new NotFoundException("COURSE_NOT_FOUND", "Enrollment not found"));
    }

    public Map<String, Object> course(UUID learner, UUID id) {
        return Map.of("enrollment", enrollmentService.enrollment(learner, id), "progress", progress(learner, id));
    }

    public Map<String, Object> progress(UUID learner, UUID id) {
        enrollmentService.enrollment(learner, id);
        var e = enrollmentEntity(learner, id);
        long done = progress.countByEnrollmentId(id), total = resources.countByVersionId(e.getVersionId());
        var quiz = assessments.evidence(e.getVersionId(), id);
        return Map.of("totalResources", total, "completedResources", done,
                "officialAssessments", quiz.requiredOfficialAssessments(),
                "passedAssessments", quiz.passedOfficialAssessments(),
                "completed", "COMPLETED".equals(e.getStatus()));
    }

    @Transactional
    public Map<String, Object> complete(UUID learner, UUID enrollmentId, UUID resource) {
        enrollmentService.enrollment(learner, enrollmentId);
        var e = enrollments.findForUpdate(enrollmentId, learner).orElseThrow(()
                -> new NotFoundException("ENROLLMENT_NOT_FOUND", "Enrollment not found"));
        var context = resources.findResourceContext(resource);
        if (context.isEmpty() || !context.get().getVersionId().equals(e.getVersionId())) {
            throw new NotFoundException("RESOURCE_NOT_IN_ENROLLMENT", "Resource does not belong to this enrollment");

        }
        var progressId = new com.skillproof.backend.course.infrastructure.ResourceProgressId(enrollmentId, resource);
        if (!progress.existsById(progressId)) {
            progress.saveAndFlush(new ResourceProgressEntity(enrollmentId, resource, Instant.now()));
        }
        evidence.evaluate(enrollmentId, 0, 0);
        return progress(learner, enrollmentId);
    }

    public void evaluate(UUID id, int required, int passed) {
        evidence.evaluate(id, required, passed);
    }
}
