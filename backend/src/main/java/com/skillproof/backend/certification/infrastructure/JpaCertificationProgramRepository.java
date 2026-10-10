package com.skillproof.backend.certification.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.skillproof.backend.certification.application.CertificationProgramRepository;
import com.skillproof.backend.certification.domain.CertificationProgram;

@Repository
public class JpaCertificationProgramRepository implements CertificationProgramRepository {

    private final CertificationProgramJpaRepository repository;

    public JpaCertificationProgramRepository(CertificationProgramJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean activeForVersion(UUID version) {
        return repository.existsByCourseVersionIdAndStatus(version, CertificationProgram.Status.ACTIVE);
    }

    @Override
    public Optional<CertificationProgram> findActiveForVersion(UUID version) {
        return repository.findByCourseVersionIdAndStatus(version, CertificationProgram.Status.ACTIVE).map(CertificationProgramEntity::domain);
    }

    public CertificationProgram save(CertificationProgram p) {
        return repository.save(new CertificationProgramEntity(p)).domain();
    }

    public Optional<CertificationProgram> findById(UUID id) {
        return repository.findById(id).map(CertificationProgramEntity::domain);
    }

    @Override
    public Optional<CertificationProgram> findForIssue(UUID id) {
        return repository.findForIssue(id).map(CertificationProgramEntity::domain);
    }

    @Override
    public List<CertificationProgram> findByOrganizationId(UUID organizationId) {
        return repository.findByOrganizationIdOrderByCreatedAtDesc(organizationId).stream()
                .map(CertificationProgramEntity::domain)
                .toList();
    }
}
