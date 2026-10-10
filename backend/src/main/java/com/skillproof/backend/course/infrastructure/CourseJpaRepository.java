package com.skillproof.backend.course.infrastructure;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseJpaRepository
        extends JpaRepository<CourseEntity, UUID> {

    @org.springframework.data.jpa.repository.Lock(
            jakarta.persistence.LockModeType.PESSIMISTIC_WRITE
    )
    @org.springframework.data.jpa.repository.Query(
            "select path from CourseEntity path where path.id = :id"
    )
    java.util.Optional<CourseEntity> findForUpdate(UUID id);

    List<CourseEntity> findByOrganizationIdOrderByCreatedAtDesc(UUID id);

    List<CourseEntity> findByCreatedByOrderByCreatedAtDesc(UUID actor);
}
