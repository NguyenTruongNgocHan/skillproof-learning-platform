package com.skillproof.backend.certification.infrastructure;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.skillproof.backend.certification.application.CertificateEligibilityRepository;
import com.skillproof.backend.certification.domain.CertificateEligibility;

@Repository
public class JpaCertificateEligibilityRepository implements CertificateEligibilityRepository {

    private final CertificateEligibilityJpaRepository repository;

    public JpaCertificateEligibilityRepository(CertificateEligibilityJpaRepository repository) {
        this.repository = repository;
    }

    public CertificateEligibility save(CertificateEligibility e) {
        return repository.save(new CertificateEligibilityEntity(e)).domain();
    }

    public Optional<CertificateEligibility> findLatest(UUID p, UUID l) {
        return repository.findFirstByCertificationProgramIdAndLearnerUserIdOrderByEvaluatedAtDesc(p, l)
                .map(CertificateEligibilityEntity::domain);
    }
}
