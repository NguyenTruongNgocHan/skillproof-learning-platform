package com.skillproof.backend.certification.infrastructure;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface CertificateJpaRepository extends JpaRepository<CertificateEntity, UUID> {
    Optional<CertificateEntity> findByCertificationProgramIdAndLearnerUserId(UUID programId, UUID learnerId);
    Optional<CertificateEntity> findBySerialNumber(String serialNumber);
}
