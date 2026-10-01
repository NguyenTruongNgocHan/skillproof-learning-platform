package com.skillproof.backend.learning.infrastructure;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ResourceProgressJpaRepository extends JpaRepository<ResourceProgressEntity, ResourceProgressId> {

    List<ResourceProgressEntity> findByEnrollmentId(UUID enrollmentId);

    long countByEnrollmentId(UUID enrollmentId);
}
