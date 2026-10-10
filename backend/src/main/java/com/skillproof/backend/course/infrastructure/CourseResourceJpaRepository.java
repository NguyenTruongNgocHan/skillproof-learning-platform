package com.skillproof.backend.course.infrastructure;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseResourceJpaRepository extends JpaRepository<CourseResourceEntity, UUID> {

    List<CourseResourceEntity> findByModuleIdOrderByPosition(UUID id);

    List<CourseResourceEntity> findByLessonIdOrderByPosition(UUID lessonId);
}
