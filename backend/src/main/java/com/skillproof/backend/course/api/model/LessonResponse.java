package com.skillproof.backend.course.api.model;

import com.skillproof.backend.course.infrastructure.persistence.LessonEntity;
import java.util.UUID;

/** Stable HTTP projection; persistence entities are never serialized by this endpoint. */
public record LessonResponse(UUID id, UUID moduleId, int position, String title, String body) {
  public static LessonResponse from(LessonEntity value) {
    return new LessonResponse(
        value.getId(), value.getModuleId(), value.getPosition(), value.getTitle(), value.getBody());
  }
}
