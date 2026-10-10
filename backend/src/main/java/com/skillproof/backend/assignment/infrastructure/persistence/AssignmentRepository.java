package com.skillproof.backend.assignment.infrastructure.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AssignmentRepository extends JpaRepository<AssignmentEntity, UUID> {

    List<AssignmentEntity> findByVersionId(UUID versionId);

    boolean existsByOwnerScopeAndOwnerId(String scope, UUID ownerId);

    long countByVersionIdAndRequiredTrue(UUID versionId);

}
