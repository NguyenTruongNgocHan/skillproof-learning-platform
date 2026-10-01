package com.skillproof.backend.organization.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface OrganizationAuthorityGrantJpaRepository extends JpaRepository<OrganizationAuthorityGrantEntity, UUID> {

    List<OrganizationAuthorityGrantEntity> findByMembershipIdAndActiveTrue(UUID membershipId);

    Optional<OrganizationAuthorityGrantEntity> findByMembershipIdAndAuthority(UUID membershipId, String authority);

    boolean existsByMembershipIdAndAuthorityAndActiveTrue(UUID membershipId, String authority);
}
