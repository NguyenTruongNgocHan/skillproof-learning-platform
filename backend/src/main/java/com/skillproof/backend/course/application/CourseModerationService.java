package com.skillproof.backend.course.application;

import com.skillproof.backend.common.exception.*;
import com.skillproof.backend.course.infrastructure.CourseJpaRepository;
import com.skillproof.backend.operations.contract.ActivityJournal;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CourseModerationService {
  private final CourseJpaRepository courses;
  private final CourseAccess access;
  private final ActivityJournal journal;

  public CourseModerationService(
      CourseJpaRepository courses, CourseAccess access, ActivityJournal journal) {
    this.courses = courses;
    this.access = access;
    this.journal = journal;
  }

  public record Decision(UUID courseId, boolean suspended, String reason) {}

  @Transactional
  public Decision moderate(UUID actor, UUID id, boolean suspended, String reason) {
    access.admin(actor);
    if (reason == null || reason.isBlank() || reason.length() > 1000)
      throw new BadRequestException(
          "MODERATION_REASON", "Provide a reason of 1 to 1000 characters");
    var course =
        courses
            .findForUpdate(id)
            .orElseThrow(() -> new NotFoundException("COURSE_NOT_FOUND", "Course not found"));
    course.moderate(suspended, reason.trim());
    journal.record(
        actor,
        course.getOrganizationId(),
        suspended ? "COURSE_SUSPENDED" : "COURSE_RESTORED",
        id,
        reason.trim());
    return new Decision(id, suspended, reason.trim());
  }

  public record Summary(
      UUID id,
      UUID organizationId,
      String slug,
      String title,
      boolean suspended,
      String moderationReason) {}

  private Summary view(com.skillproof.backend.course.infrastructure.CourseEntity course) {
    return new Summary(
        course.getId(),
        course.getOrganizationId(),
        course.getSlug(),
        course.getTitle(),
        course.isSuspended(),
        course.getModerationReason());
  }

  @Transactional(readOnly = true)
  public com.skillproof.backend.common.api.PageResult<Summary> search(
      UUID actor, String query, Boolean suspended, int page, int size) {
    access.admin(actor);
    var q = query.trim().toLowerCase(java.util.Locale.ROOT);
    var rows =
        courses
            .findAll(
                org.springframework.data.domain.Sort.by("createdAt")
                    .descending()
                    .and(org.springframework.data.domain.Sort.by("id")))
            .stream()
            .filter(c -> suspended == null || suspended == c.isSuspended())
            .filter(
                c ->
                    (c.getTitle() + " " + c.getSlug())
                        .toLowerCase(java.util.Locale.ROOT)
                        .contains(q))
            .map(this::view)
            .toList();
    return com.skillproof.backend.common.api.PageResult.of(rows, page, size);
  }

  @Transactional(readOnly = true)
  public Summary detail(UUID actor, UUID id) {
    access.admin(actor);
    return view(
        courses
            .findById(id)
            .orElseThrow(() -> new NotFoundException("COURSE_NOT_FOUND", "Course not found")));
  }
}
