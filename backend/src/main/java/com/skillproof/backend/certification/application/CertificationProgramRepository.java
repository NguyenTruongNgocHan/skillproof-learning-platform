package com.skillproof.backend.certification.application;

import java.util.Optional;
import java.util.UUID;

import com.skillproof.backend.certification.domain.CertificationProgram;

public interface CertificationProgramRepository {

    CertificationProgram save(CertificationProgram program);

    Optional<CertificationProgram> findById(UUID programId);
}
