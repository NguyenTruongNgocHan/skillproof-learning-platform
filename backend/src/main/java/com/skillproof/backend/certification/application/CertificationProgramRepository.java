package com.skillproof.backend.certification.application;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.skillproof.backend.certification.domain.CertificationProgram;

public interface CertificationProgramRepository {

    boolean activeForVersion(UUID versionId);

    Optional<CertificationProgram> findActiveForVersion(UUID versionId);

    CertificationProgram save(CertificationProgram program);

    Optional<CertificationProgram> findById(UUID programId);

    Optional<CertificationProgram> findForIssue(UUID programId);

    List<CertificationProgram> findByOrganizationId(UUID organizationId);
}
