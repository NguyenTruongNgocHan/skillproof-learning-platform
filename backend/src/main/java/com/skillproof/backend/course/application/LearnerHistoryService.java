package com.skillproof.backend.course.application;

import com.skillproof.backend.assignment.contract.LearnerAssignmentHistoryQuery;
import com.skillproof.backend.course.infrastructure.*;
import com.skillproof.backend.identity.contract.IdentityAccessQuery;
import com.skillproof.backend.quiz.contract.LearnerAssessmentHistoryQuery;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LearnerHistoryService {
  private final CourseEnrollmentRepository enrollments;
  private final CourseContextRepository versions;
  private final IdentityAccessQuery identity;
  private final CompletionEvidenceStore completion;
  private final LearnerAssessmentHistoryQuery assessments;
  private final LearnerAssignmentHistoryQuery assignments;

  public LearnerHistoryService(
      CourseEnrollmentRepository enrollments,
      CourseContextRepository versions,
      IdentityAccessQuery identity,
      CompletionEvidenceStore completion,
      LearnerAssessmentHistoryQuery assessments,
      LearnerAssignmentHistoryQuery assignments) {
    this.enrollments = enrollments;
    this.versions = versions;
    this.identity = identity;
    this.completion = completion;
    this.assessments = assessments;
    this.assignments = assignments;
  }

  @Transactional(readOnly = true)
  public Map<String, Object> history(UUID learner, UUID id) {
    if (!identity.isActiveLearner(learner))
      throw new org.springframework.security.access.AccessDeniedException(
          "Active learner required");
    var e =
        enrollments
            .findByIdAndLearnerId(id, learner)
            .orElseThrow(
                () ->
                    new com.skillproof.backend.common.exception.NotFoundException(
                        "ENROLLMENT_NOT_FOUND", "Enrollment not found"));
    var version = versions.findById(e.getVersionId()).orElseThrow();
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("enrollmentId", id);
    data.put("courseId", e.getCourseId());
    data.put("versionId", e.getVersionId());
    data.put("versionNo", version.getVersionNo());
    data.put("title", version.getPublishedTitle());
    data.put("status", e.getStatus());
    data.put("completedAt", e.getCompletedAt());
    data.put("completionEvidenceJson", completion.latestEvidenceJson(id).orElse(null));
    data.put("assessments", assessments.history(learner, id, e.getVersionId()));
    data.put("assignments", assignments.history(learner, id, e.getVersionId()));
    return data;
  }
}
