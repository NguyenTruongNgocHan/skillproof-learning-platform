package com.skillproof.backend.learningpath.infrastructure.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LearningPathItemRepository extends JpaRepository<LearningPathItemEntity, UUID> {

    List<LearningPathItemEntity> findByPathIdOrderByPosition(UUID pathId);

    boolean existsByPrerequisiteItemId(UUID prerequisiteId);

}
