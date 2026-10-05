package com.skillproof.backend.certification.infrastructure;

import java.util.UUID;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import jakarta.persistence.LockModeType;

interface CertificationProgramJpaRepository extends JpaRepository<CertificationProgramEntity, UUID> {
    List<CertificationProgramEntity> findByOrganizationIdOrderByCreatedAtDesc(UUID organizationId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select program from CertificationProgramEntity program where program.id = :id")
    Optional<CertificationProgramEntity> findForIssue(UUID id);
}
