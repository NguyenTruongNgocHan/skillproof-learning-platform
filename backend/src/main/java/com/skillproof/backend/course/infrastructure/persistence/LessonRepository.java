package com.skillproof.backend.course.infrastructure.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface LessonRepository extends JpaRepository<LessonEntity, UUID> {

    List<LessonEntity> findByModuleIdOrderByPosition(UUID moduleId);

    @Query("select count(l) from LessonEntity l, com.skillproof.backend.course.infrastructure.CourseModuleEntity m where l.moduleId = m.id and m.versionId = :versionId")
    long countByVersionId(UUID versionId);

    long countByModuleId(UUID moduleId);

}
