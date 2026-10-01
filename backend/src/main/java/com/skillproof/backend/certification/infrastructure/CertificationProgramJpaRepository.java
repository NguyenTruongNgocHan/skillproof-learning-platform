package com.skillproof.backend.certification.infrastructure;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface CertificationProgramJpaRepository extends JpaRepository<CertificationProgramEntity, UUID> {
}
