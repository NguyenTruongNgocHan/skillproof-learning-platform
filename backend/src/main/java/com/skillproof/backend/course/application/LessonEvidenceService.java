package com.skillproof.backend.course.application;

import com.skillproof.backend.course.contract.LessonEvidenceQuery;
import com.skillproof.backend.course.infrastructure.persistence.LessonProgressRepository;
import com.skillproof.backend.course.infrastructure.persistence.LessonRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class LessonEvidenceService implements LessonEvidenceQuery {

  private final LessonRepository lessons;
  private final LessonProgressRepository progress;

  public LessonEvidenceService(LessonRepository lessons, LessonProgressRepository progress) {
    this.lessons = lessons;
    this.progress = progress;
  }

  @Override
  public Evidence evidence(UUID version, UUID enrollment) {
    return new Evidence(
        Math.toIntExact(lessons.countByVersionId(version)),
        Math.toIntExact(progress.countByEnrollmentId(enrollment)));
  }
}
