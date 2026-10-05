package com.skillproof.backend.certification.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.skillproof.backend.certification.application.CertificateRepository;
import com.skillproof.backend.certification.domain.Certificate;

@Repository
public class JpaCertificateRepository implements CertificateRepository {

    private final CertificateJpaRepository repository;

    public JpaCertificateRepository(CertificateJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Certificate save(Certificate value) {
        return repository.save(new CertificateEntity(value)).domain();
    }

    @Override
    public Optional<Certificate> findById(UUID id) {
        return repository.findById(id).map(CertificateEntity::domain);
    }

    @Override
    public Optional<Certificate> findByProgramAndLearner(
        UUID programId,
        UUID learnerId
    ) {
        return repository
            .findByCertificationProgramIdAndLearnerUserId(programId, learnerId)
            .map(CertificateEntity::domain);
    }

    @Override
    public Optional<Certificate> findBySerial(String serialNumber) {
        return repository
            .findBySerialNumber(serialNumber)
            .map(CertificateEntity::domain);
    }

    @Override
    public List<Certificate> findByProgramIds(List<UUID> programIds) {
        if (programIds.isEmpty()) return List.of();
        return repository
            .findByCertificationProgramIdInOrderByIssuedAtDesc(programIds)
            .stream()
            .map(CertificateEntity::domain)
            .toList();
    }

    @Override
    public Page<Certificate> searchByProgramIds(
        List<UUID> programIds,
        Certificate.Status status,
        UUID learnerId,
        String pattern,
        Pageable pageable
    ) {
        return repository
            .search(programIds, status, learnerId, pattern, pageable)
            .map(CertificateEntity::domain);
    }
}
