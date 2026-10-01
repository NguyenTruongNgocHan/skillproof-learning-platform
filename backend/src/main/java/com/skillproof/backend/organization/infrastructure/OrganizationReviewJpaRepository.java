package com.skillproof.backend.organization.infrastructure;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface OrganizationReviewJpaRepository extends JpaRepository<OrganizationReviewEntity, UUID> {

    List<OrganizationReviewEntity> findByOrganizationIdOrderByReviewedAtDesc(UUID organizationId);
}
