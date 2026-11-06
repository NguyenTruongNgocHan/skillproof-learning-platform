package com.skillproof.backend.course.application;

import com.skillproof.backend.common.exception.NotFoundException;
import com.skillproof.backend.course.infrastructure.CourseEnrollmentRepository;
import com.skillproof.backend.course.infrastructure.CourseModuleJpaRepository;
import com.skillproof.backend.course.infrastructure.CourseResourceJpaRepository;
import com.skillproof.backend.course.infrastructure.persistence.LessonRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CourseContentQueryService {

  private final CourseEnrollmentRepository enrollments;
  private final CourseEnrollmentService enrollmentAccess;
  private final CourseModuleJpaRepository modules;
  private final LessonRepository lessons;
  private final CourseResourceJpaRepository resources;

  @org.springframework.beans.factory.annotation.Autowired
  private com.skillproof.backend.course.infrastructure.ResourceProgressJpaRepository
      resourceProgress;

  @org.springframework.beans.factory.annotation.Autowired
  private com.skillproof.backend.course.infrastructure.persistence.LessonProgressRepository
      lessonProgress;

  public CourseContentQueryService(
      CourseEnrollmentRepository enrollments,
      CourseEnrollmentService enrollmentAccess,
      CourseModuleJpaRepository modules,
      LessonRepository lessons,
      CourseResourceJpaRepository resources) {
    this.enrollments = enrollments;
    this.enrollmentAccess = enrollmentAccess;
    this.modules = modules;
    this.lessons = lessons;
    this.resources = resources;
  }

  public record Resource(
      UUID id,
      int position,
      String kind,
      String title,
      String body,
      String url,
      boolean required,
      boolean completed) {}

  public record Lesson(
      UUID id,
      int position,
      String title,
      String body,
      boolean completed,
      List<Resource> resources) {}

  public record Module(UUID id, int position, String title, List<Lesson> lessons) {}

  @Transactional(readOnly = true)
  public List<Module> enrolled(UUID learner, UUID enrollmentId) {
    enrollmentAccess.enrollment(learner, enrollmentId);
    var enrollment =
        enrollments
            .findByIdAndLearnerId(enrollmentId, learner)
            .orElseThrow(
                () -> new NotFoundException("ENROLLMENT_NOT_FOUND", "Enrollment not found"));
    return modules.findByVersionIdOrderByPosition(enrollment.getVersionId()).stream()
        .map(
            m ->
                new Module(
                    m.getId(),
                    m.getPosition(),
                    m.getTitle(),
                    lessons.findByModuleIdOrderByPosition(m.getId()).stream()
                        .map(
                            l ->
                                new Lesson(
                                    l.getId(),
                                    l.getPosition(),
                                    l.getTitle(),
                                    l.getBody(),
                                    lessonProgress.existsByEnrollmentIdAndLessonId(
                                        enrollmentId, l.getId()),
                                    resources.findByLessonIdOrderByPosition(l.getId()).stream()
                                        .map(
                                            r ->
                                                new Resource(
                                                    r.getId(),
                                                    r.getPosition(),
                                                    r.getKind(),
                                                    r.getTitle(),
                                                    r.getBody(),
                                                    r.getUrl(),
                                                    r.isRequiredForCompletion(),
                                                    resourceProgress.existsById(
                                                        new com.skillproof.backend.course
                                                            .infrastructure.ResourceProgressId(
                                                            enrollmentId, r.getId()))))
                                        .toList()))
                        .toList()))
        .toList();
  }
}
