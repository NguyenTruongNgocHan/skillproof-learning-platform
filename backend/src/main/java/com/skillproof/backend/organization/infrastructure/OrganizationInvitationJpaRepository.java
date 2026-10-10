package com.skillproof.backend.organization.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import jakarta.persistence.LockModeType;

public interface OrganizationInvitationJpaRepository
        extends JpaRepository<OrganizationInvitationEntity, UUID> {

    List<OrganizationInvitationEntity> findByOrganizationIdOrderByCreatedAtDesc(UUID organizationId);

    Optional<OrganizationInvitationEntity> findByTokenHash(String tokenHash);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select invitation from OrganizationInvitationEntity invitation where invitation.id = :id")
    Optional<OrganizationInvitationEntity> findForUpdate(UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select invitation from OrganizationInvitationEntity invitation where invitation.tokenHash = :tokenHash")
    Optional<OrganizationInvitationEntity> findByTokenHashForUpdate(String tokenHash);

    Optional<OrganizationInvitationEntity> findFirstByOrganizationIdAndEmailAndStatusOrderByCreatedAtDesc(
            UUID organizationId, String email, String status);
}
