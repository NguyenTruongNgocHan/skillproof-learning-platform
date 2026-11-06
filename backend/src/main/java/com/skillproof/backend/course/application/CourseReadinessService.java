package com.skillproof.backend.course.application;

import com.skillproof.backend.course.contract.*;
import com.skillproof.backend.course.infrastructure.*;
import com.skillproof.backend.course.infrastructure.persistence.LessonRepository;
import java.util.*;
import org.springframework.stereotype.Service;

@Service
public class CourseReadinessService {
  @org.springframework.beans.factory.annotation.Autowired
  private com.skillproof.backend.assignment.contract.AssignmentVersionBridge assignments;

  private final CourseAccess access;
  private final CourseContextRepository versions;
  private final CourseModuleJpaRepository modules;
  private final CourseResourceJpaRepository resources;
  private final LessonRepository lessons;
  private final CompletionPolicyJpaRepository policies;
  private final CourseMediaDependency media;
  private final CourseAssessmentDependency assessments;

  public CourseReadinessService(
      CourseAccess access,
      CourseContextRepository versions,
      CourseModuleJpaRepository modules,
      CourseResourceJpaRepository resources,
      LessonRepository lessons,
      CompletionPolicyJpaRepository policies,
      CourseMediaDependency media,
      CourseAssessmentDependency assessments) {
    this.access = access;
    this.versions = versions;
    this.modules = modules;
    this.resources = resources;
    this.lessons = lessons;
    this.policies = policies;
    this.media = media;
    this.assessments = assessments;
  }

  @org.springframework.transaction.annotation.Transactional(readOnly = true)
  public Map<String, Object> check(UUID actor, UUID id) {
    var v =
        versions
            .findById(id)
            .orElseThrow(
                () ->
                    new com.skillproof.backend.common.exception.NotFoundException(
                        "VERSION_NOT_FOUND", "Version not found"));
    if (!access.canReview(actor))
      access.manage(actor, versions.findCourseByVersionId(id).orElseThrow());
    List<Map<String, Object>> issues = issues(id);
    return Map.of("ready", issues.isEmpty(), "status", v.getStatus(), "issues", issues);
  }

  /** Called under the publication version lock as well as by the readiness query. */
  public List<Map<String, Object>> issues(UUID id) {
    List<Map<String, Object>> issues = new ArrayList<>();
    var version = versions.findById(id).orElseThrow();
    var course = versions.findCourseByVersionId(id).orElseThrow();
    if (course.isSuspended())
      issues.add(
          Map.of(
              "code",
              "COURSE_SUSPENDED",
              "tab",
              "publish",
              "message",
              "Resolve platform suspension before publishing"));
    if (course.getOrganizationId() == null && !"APPROVED".equals(version.getReviewStatus()))
      issues.add(
          Map.of(
              "code",
              "COURSE_REVIEW_REQUIRED",
              "tab",
              "publish",
              "message",
              "Community course requires approval"));
    if (assignments.hasExpiredDeadline(id))
      issues.add(
          Map.of(
              "code",
              "ASSIGNMENT_DEADLINE",
              "tab",
              "assignments",
              "message",
              "Update expired assignment deadlines before publishing"));
    if (lessons.countByVersionId(id) == 0)
      issues.add(
          Map.of("code", "COURSE_EMPTY", "tab", "outline", "message", "Add at least one lesson"));
    for (var m : modules.findByVersionIdOrderByPosition(id)) {
      for (var l : lessons.findByModuleIdOrderByPosition(m.getId())) {
        var files = resources.findByLessonIdOrderByPosition(l.getId());
        if (l.getBody().isBlank() && files.isEmpty())
          issues.add(
              Map.of(
                  "code",
                  "LESSON_EMPTY",
                  "tab",
                  "outline",
                  "targetId",
                  l.getId(),
                  "message",
                  "Lesson needs text or a resource"));
      }
      for (var r : resources.findByModuleIdOrderByPosition(m.getId())) {
        if (r.getLessonId() == null)
          issues.add(
              Map.of(
                  "code",
                  "LESSON_REQUIRED",
                  "tab",
                  "outline",
                  "targetId",
                  r.getId(),
                  "message",
                  "Resource must belong to a lesson"));
        if ((Set.of("FILE", "AUDIO", "IMAGE").contains(r.getKind())
                || ("VIDEO".equals(r.getKind()) && r.getUrl() == null))
            && !media.hasMatchingAsset(r.getId(), r.getKind()))
          issues.add(
              Map.of(
                  "code",
                  "MEDIA_REQUIRED",
                  "tab",
                  "outline",
                  "targetId",
                  r.getId(),
                  "message",
                  "Upload a ready file for " + r.getTitle()));
      }
    }
    if (assessments.hasDraftAssessments(id))
      issues.add(
          Map.of(
              "code",
              "ASSESSMENT_DRAFT",
              "tab",
              "assessments",
              "message",
              "Publish or remove draft assessments"));
    if (policies.findById(id).orElseThrow().isRequireOfficialAssessments()
        && !assessments.hasPublishedOfficialAssessment(id))
      issues.add(
          Map.of(
              "code",
              "OFFICIAL_REQUIRED",
              "tab",
              "assessments",
              "message",
              "Publish an official assessment"));
    return List.copyOf(issues);
  }
}
