package com.skillproof.backend.course.infrastructure;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseModuleJpaRepository extends JpaRepository<CourseModuleEntity, UUID> {

    List<CourseModuleEntity> findByVersionIdOrderByPosition(UUID id);
}
