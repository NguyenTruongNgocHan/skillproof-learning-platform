package com.skillproof.backend.assignment.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AssignmentRepository extends JpaRepository<AssignmentEntity, UUID> {

    org.springframework.data.domain.Page<AssignmentEntity> findByVersionIdIn(
            java.util.Collection<UUID> versions, org.springframework.data.domain.Pageable page);

    List<AssignmentEntity> findByVersionId(UUID versionId);

    boolean existsByOwnerScopeAndOwnerId(String scope, UUID ownerId);

    long countByVersionIdAndRequiredTrue(UUID versionId);
}
