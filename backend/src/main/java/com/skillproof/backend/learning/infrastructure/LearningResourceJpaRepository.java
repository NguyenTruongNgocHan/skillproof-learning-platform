package com.skillproof.backend.learning.infrastructure;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LearningResourceJpaRepository extends JpaRepository<LearningResourceEntity, UUID> {

    List<LearningResourceEntity> findByModuleIdOrderByPosition(UUID id);
}
