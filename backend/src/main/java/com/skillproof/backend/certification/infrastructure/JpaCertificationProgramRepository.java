package com.skillproof.backend.certification.infrastructure;

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

    public CertificationProgram save(CertificationProgram p) {
        return repository.save(new CertificationProgramEntity(p)).domain();
    }

    public Optional<CertificationProgram> findById(UUID id) {
        return repository.findById(id).map(CertificationProgramEntity::domain);
    }
}
