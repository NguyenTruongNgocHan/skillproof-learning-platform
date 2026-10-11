package com.skillproof.backend.course.application;

import com.skillproof.backend.course.contract.CourseQuizAccess;
import com.skillproof.backend.course.infrastructure.*;
import com.skillproof.backend.identity.contract.IdentityAccessQuery;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Service
public class OrganizerEnrollmentService {
  @org.springframework.beans.factory.annotation.Autowired private CourseModuleJpaRepository modules;

  @org.springframework.beans.factory.annotation.Autowired
  private CourseResourceJpaRepository resourceRows;

  @org.springframework.beans.factory.annotation.Autowired
  private com.skillproof.backend.course.infrastructure.persistence.LessonRepository lessons;

  @org.springframework.beans.factory.annotation.Autowired
  private com.skillproof.backend.course.infrastructure.persistence.LessonProgressRepository
      lessonProgress;

  @org.springframework.beans.factory.annotation.Autowired
  private com.skillproof.backend.quiz.contract.QuizCompletionEvidenceQuery quiz;

  @org.springframework.beans.factory.annotation.Autowired
  private com.skillproof.backend.assignment.contract.AssignmentEvidenceQuery assignments;

  @org.springframework.beans.factory.annotation.Autowired
  private com.skillproof.backend.access.contract.LearnerAccessHistoryQuery accessHistory;

  @org.springframework.beans.factory.annotation.Autowired
  private com.skillproof.backend.certification.contract.LearnerCourseCredentialsQuery credentials;

  @org.springframework.beans.factory.annotation.Autowired
  private CompletionEvidenceStore completion;

  @org.springframework.beans.factory.annotation.Autowired
  private com.skillproof.backend.quiz.contract.LearnerAssessmentHistoryQuery assessmentHistory;

  @org.springframework.beans.factory.annotation.Autowired
  private com.skillproof.backend.assignment.contract.LearnerAssignmentHistoryQuery
      assignmentHistory;

  private final CourseEnrollmentRepository enrollments;
  private final CourseQuizAccess access;
  private final IdentityAccessQuery identity;
  private final ResourceProgressJpaRepository progress;
  private final CourseResourceContextRepository resources;

  public OrganizerEnrollmentService(
      CourseEnrollmentRepository enrollments,
      CourseQuizAccess access,
      IdentityAccessQuery identity,
      ResourceProgressJpaRepository progress,
      CourseResourceContextRepository resources) {
    this.enrollments = enrollments;
    this.access = access;
    this.identity = identity;
    this.progress = progress;
    this.resources = resources;
  }

  public Page<Map<String, Object>> list(UUID actor, UUID version, String query, int page) {
    return list(
        actor,
        version,
        query,
        page,
        "",
        java.time.Instant.parse("1970-01-01T00:00:00Z"),
        java.time.Instant.parse("9999-12-31T00:00:00Z"));
  }

  @org.springframework.transaction.annotation.Transactional(readOnly = true)
  public Page<Map<String, Object>> list(
      UUID actor,
      UUID version,
      String query,
      int page,
      String status,
      java.time.Instant start,
      java.time.Instant end) {
    access.requireVersionAuthor(actor, version);
    if (!Set.of("", "ACTIVE", "COMPLETED").contains(status) || !end.isAfter(start))
      throw new com.skillproof.backend.common.exception.BadRequestException(
          "ENROLLMENT_FILTER", "Invalid status or date range");
    var ids = new ArrayList<>(identity.findAccountIdsByEmailFragment(query));
    if (ids.isEmpty()) ids.add(new UUID(0, 0));
    return enrollments
        .workspaceSearch(
            version, query, ids, status, start, end, PageRequest.of(Math.max(0, page), 100))
        .map(
            e -> {
              Map<String, Object> r = new LinkedHashMap<>();
              r.put("id", e.getId());
              r.put("learnerId", e.getLearnerId());
              r.put(
                  "email",
                  identity
                      .find(e.getLearnerId())
                      .map(IdentityAccessQuery.Account::email)
                      .orElse(""));
              r.put("status", e.getStatus());
              r.put("enrolledAt", e.getEnrolledAt());
              r.put("completedAt", e.getCompletedAt());
              r.put("retainedFreeAccess", e.isRetainedFreeAccess());
              r.put("completedResources", progress.countByEnrollmentId(e.getId()));
              r.put("totalResources", resources.countByVersionId(version));
              return r;
            });
  }

  @org.springframework.transaction.annotation.Transactional(readOnly = true)
  public Map<String, Object> detail(UUID actor, UUID version, UUID enrollment) {
    access.requireVersionAuthor(actor, version);
    var e =
        enrollments
            .findById(enrollment)
            .filter(row -> row.getVersionId().equals(version))
            .orElseThrow(
                () ->
                    new com.skillproof.backend.common.exception.NotFoundException(
                        "ENROLLMENT_NOT_FOUND", "Enrollment not found in this version"));
    var completed =
        progress.findByEnrollmentId(enrollment).stream()
            .collect(
                java.util.stream.Collectors.toMap(
                    ResourceProgressEntity::getResourceId, ResourceProgressEntity::getCompletedAt));
    var doneLessons =
        lessonProgress.findByEnrollmentId(enrollment).stream()
            .map(x -> x.getLessonId())
            .collect(java.util.stream.Collectors.toSet());
    List<Map<String, Object>> outline =
        modules.findByVersionIdOrderByPosition(version).stream()
            .map(
                m ->
                    Map.<String, Object>of(
                        "title",
                        m.getTitle(),
                        "lessons",
                        lessons.findByModuleIdOrderByPosition(m.getId()).stream()
                            .map(
                                l ->
                                    Map.of(
                                        "id",
                                        l.getId(),
                                        "title",
                                        l.getTitle(),
                                        "completed",
                                        doneLessons.contains(l.getId())))
                            .toList(),
                        "resources",
                        resourceRows.findByModuleIdOrderByPosition(m.getId()).stream()
                            .map(
                                r -> {
                                  Map<String, Object> item = new LinkedHashMap<>();
                                  item.put("id", r.getId());
                                  item.put("title", r.getTitle());
                                  item.put("kind", r.getKind());
                                  item.put("required", r.isRequiredForCompletion());
                                  item.put("completedAt", completed.get(r.getId()));
                                  return item;
                                })
                            .toList()))
            .toList();
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("enrollmentId", enrollment);
    result.put("learnerId", e.getLearnerId());
    result.put(
        "email",
        identity.find(e.getLearnerId()).map(IdentityAccessQuery.Account::email).orElse(""));
    result.put("status", e.getStatus());
    result.put("enrolledAt", e.getEnrolledAt());
    result.put("completedAt", e.getCompletedAt());
    result.put("retainedFreeAccess", e.isRetainedFreeAccess());
    result.put("accessHistory", accessHistory.history(e.getLearnerId(), version));
    result.put("certificates", credentials.credentials(e.getLearnerId(), version));
    result.put("completionEvidenceJson", completion.latestEvidenceJson(enrollment).orElse(null));
    result.put("outline", outline);
    result.put("assessments", quiz.evidence(version, enrollment));
    result.put("assignments", assignments.evidence(version, enrollment));
    result.put(
        "assessmentHistory", assessmentHistory.history(e.getLearnerId(), enrollment, version));
    result.put(
        "assignmentHistory", assignmentHistory.history(e.getLearnerId(), enrollment, version));
    return result;
  }
}
