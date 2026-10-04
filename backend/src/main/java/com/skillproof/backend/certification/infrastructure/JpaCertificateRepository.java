package com.skillproof.backend.certification.infrastructure;

import com.skillproof.backend.certification.application.CertificateRepository;
import com.skillproof.backend.certification.domain.Certificate;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class JpaCertificateRepository implements CertificateRepository {
    private final CertificateJpaRepository repository;

    public JpaCertificateRepository(CertificateJpaRepository repository) {
        this.repository = repository;
    }

    @Override public Certificate save(Certificate value) {
        return repository.save(new CertificateEntity(value)).domain();
    }
    @Override public Optional<Certificate> findById(UUID id) {
        return repository.findById(id).map(CertificateEntity::domain);
    }
    @Override public Optional<Certificate> findByProgramAndLearner(UUID programId, UUID learnerId) {
        return repository.findByCertificationProgramIdAndLearnerUserId(programId, learnerId)
                .map(CertificateEntity::domain);
    }
    @Override public Optional<Certificate> findBySerial(String serialNumber) {
        return repository.findBySerialNumber(serialNumber).map(CertificateEntity::domain);
    }
}
