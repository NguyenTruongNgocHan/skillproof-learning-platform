package com.skillproof.backend.course.application;

import com.skillproof.backend.course.contract.CourseWorkspaceQuery;
import com.skillproof.backend.course.infrastructure.*;
import java.util.*;
import org.springframework.stereotype.Service;

@Service
public class CourseWorkspaceQueryService implements CourseWorkspaceQuery {
  @org.springframework.beans.factory.annotation.Autowired
  private com.skillproof.backend.course.infrastructure.persistence.LessonProgressRepository
      lessonProgress;

  private final CourseContextRepository versions;
  private final CourseJpaRepository courses;
  private final CourseEnrollmentRepository enrollments;
  private final ResourceProgressJpaRepository progress;
  private final CourseAccess access;

  public CourseWorkspaceQueryService(
      CourseContextRepository versions,
      CourseJpaRepository courses,
      CourseEnrollmentRepository enrollments,
      ResourceProgressJpaRepository progress,
      CourseAccess access) {
    this.versions = versions;
    this.courses = courses;
    this.enrollments = enrollments;
    this.progress = progress;
    this.access = access;
  }

  public void lockCourse(UUID id) {
    courses.findForUpdate(id).orElseThrow();
  }

  public Context context(UUID id) {
    var v = versions.findById(id).orElseThrow();
    var c = courses.findById(v.getCourseId()).orElseThrow();
    return new Context(
        c.getId(),
        id,
        c.getOrganizationId(),
        c.getCreatedBy(),
        v.getStatus(),
        "DRAFT".equals(v.getStatus()) ? c.getTitle() : v.getPublishedTitle(),
        v.getPriceVnd(),
        v.getCertificationPriceVnd(),
        v.getAccessMode());
  }

  public List<UUID> versions(UUID org) {
    return courses.findByOrganizationIdOrderByCreatedAtDesc(org).stream()
        .flatMap(c -> versions.findByCourseIdOrderByVersionNoDesc(c.getId()).stream())
        .map(CourseVersionEntity::getId)
        .toList();
  }

  public boolean hasExperience(UUID actor, UUID version) {
    var c = context(version);
    return enrollments
        .findByLearnerIdAndCourseId(actor, c.courseId())
        .filter(
            e ->
                e.getVersionId().equals(version)
                    && ("COMPLETED".equals(e.getStatus())
                        || progress.countByEnrollmentId(e.getId()) > 0
                        || lessonProgress.countByEnrollmentId(e.getId()) > 0))
        .isPresent();
  }

  public UUID versionForCourse(UUID id) {
    return versions
        .findFirstByCourseIdAndStatusOrderByVersionNoDesc(id, "PUBLISHED")
        .orElseThrow()
        .getId();
  }

  public void requireCourseAuthor(UUID actor, UUID course) {
    access.manage(actor, courses.findById(course).orElseThrow());
  }

  public void requireAuthor(UUID actor, UUID version) {
    var v = versions.findById(version).orElseThrow();
    access.manage(actor, courses.findById(v.getCourseId()).orElseThrow());
  }

  @org.springframework.beans.factory.annotation.Autowired
  private com.skillproof.backend.organization.contract.OrganizationAuthorityQuery authorities;

  public void requireGrader(UUID actor, UUID version) {
    var c = context(version);
    if (c.organizationId() == null) {
      requireAuthor(actor, version);
      return;
    }
    if (!authorities.hasAuthority(c.organizationId(), actor, "GRADE_ASSIGNMENTS"))
      throw new org.springframework.security.access.AccessDeniedException(
          "Grading authority required");
  }
}
