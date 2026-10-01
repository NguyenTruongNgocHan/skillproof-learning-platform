package com.skillproof.backend.learning.infrastructure;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LearningPathJpaRepository extends JpaRepository<LearningPathEntity, UUID> {

    List<LearningPathEntity> findByOrganizationIdOrderByCreatedAtDesc(UUID id);
}
