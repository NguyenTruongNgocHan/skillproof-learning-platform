package com.skillproof.backend.course.infrastructure.persistence;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LessonProgressRepository extends JpaRepository<LessonProgressEntity, UUID> {

    boolean existsByEnrollmentIdAndLessonId(UUID enrollmentId, UUID lessonId);

    long countByEnrollmentId(UUID enrollmentId);

}
