package com.skillproof.backend.organization.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import jakarta.persistence.LockModeType;

public interface OrganizationApplicationRevisionJpaRepository
        extends JpaRepository<OrganizationApplicationRevisionEntity, UUID> {

    List<OrganizationApplicationRevisionEntity> findByOrganizationIdOrderByRevisionNoDesc(UUID organizationId);

    Optional<OrganizationApplicationRevisionEntity> findFirstByOrganizationIdOrderByRevisionNoDesc(UUID organizationId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select revision from OrganizationApplicationRevisionEntity revision where revision.id = :id")
    Optional<OrganizationApplicationRevisionEntity> findForUpdate(UUID id);
}
