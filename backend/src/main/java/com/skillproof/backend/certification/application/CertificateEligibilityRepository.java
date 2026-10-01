package com.skillproof.backend.certification.application;

import java.util.Optional;
import java.util.UUID;

import com.skillproof.backend.certification.domain.CertificateEligibility;

public interface CertificateEligibilityRepository {

    CertificateEligibility save(CertificateEligibility eligibility);

    Optional<CertificateEligibility> findLatest(UUID programId, UUID learnerId);
}
