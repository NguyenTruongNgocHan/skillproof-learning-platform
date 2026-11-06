package com.skillproof.backend.certification.application;

import com.skillproof.backend.certification.domain.CertificateEligibility;
import java.util.Optional;
import java.util.UUID;

public interface CertificateEligibilityRepository {

  CertificateEligibility save(CertificateEligibility eligibility);

  Optional<CertificateEligibility> findLatest(UUID programId, UUID learnerId);

  Optional<CertificateEligibility> findById(UUID id);
}
