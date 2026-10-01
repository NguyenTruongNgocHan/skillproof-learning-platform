package com.skillproof.backend.organization.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import com.skillproof.backend.organization.domain.Organization;

import jakarta.persistence.LockModeType;

interface OrganizationJpaRepository extends JpaRepository<OrganizationEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select organization from OrganizationEntity organization where organization.id = :id")
    Optional<OrganizationEntity> findForUpdate(UUID id);

    Optional<OrganizationEntity> findByOwnerUserId(UUID ownerUserId);

    List<OrganizationEntity> findByStatusOrderByCreatedAtAsc(Organization.Status status);

    @Query("""
            select organization from OrganizationEntity organization, OrganizationMembershipEntity membership
            where membership.organizationId = organization.id
              and membership.userId = :userId and membership.active = true
            order by organization.createdAt
            """)
    List<OrganizationEntity> findMembershipOrganizations(UUID userId);
}
