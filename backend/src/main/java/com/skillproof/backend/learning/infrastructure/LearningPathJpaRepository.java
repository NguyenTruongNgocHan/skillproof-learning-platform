package com.skillproof.backend.learning.infrastructure;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LearningPathJpaRepository
    extends JpaRepository<LearningPathEntity, UUID>
{
    @org.springframework.data.jpa.repository.Lock(
        jakarta.persistence.LockModeType.PESSIMISTIC_WRITE
    )
    @org.springframework.data.jpa.repository.Query(
        "select path from LearningPathEntity path where path.id = :id"
    )
    java.util.Optional<LearningPathEntity> findForUpdate(UUID id);

    List<LearningPathEntity> findByOrganizationIdOrderByCreatedAtDesc(UUID id);
}
