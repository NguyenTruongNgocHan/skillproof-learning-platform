package com.skillproof.backend.certification.application;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

import com.skillproof.backend.certification.domain.CertificationProgram;

public interface CertificationProgramRepository {

    CertificationProgram save(CertificationProgram program);

    Optional<CertificationProgram> findById(UUID programId);
    Optional<CertificationProgram> findForIssue(UUID programId);

    List<CertificationProgram> findByOrganizationId(UUID organizationId);
}
