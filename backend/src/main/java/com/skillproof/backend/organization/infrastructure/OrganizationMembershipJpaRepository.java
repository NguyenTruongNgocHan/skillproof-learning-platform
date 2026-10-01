package com.skillproof.backend.organization.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface OrganizationMembershipJpaRepository extends JpaRepository<OrganizationMembershipEntity, UUID> {

    List<OrganizationMembershipEntity> findByOrganizationId(UUID organizationId);

    Optional<OrganizationMembershipEntity> findByOrganizationIdAndUserId(UUID organizationId, UUID userId);

    boolean existsByOrganizationIdAndUserIdAndActiveTrue(UUID organizationId, UUID userId);
}
