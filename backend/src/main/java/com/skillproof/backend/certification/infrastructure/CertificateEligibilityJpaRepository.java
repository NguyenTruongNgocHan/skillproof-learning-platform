package com.skillproof.backend.certification.infrastructure;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface CertificateEligibilityJpaRepository extends JpaRepository<CertificateEligibilityEntity, UUID> {

    Optional<CertificateEligibilityEntity> findFirstByCertificationProgramIdAndLearnerUserIdOrderByEvaluatedAtDesc(UUID programId, UUID learnerId);
}
