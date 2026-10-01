package com.skillproof.backend.learning.infrastructure;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LearningModuleJpaRepository extends JpaRepository<LearningModuleEntity, UUID> {

    List<LearningModuleEntity> findByVersionIdOrderByPosition(UUID id);
}
