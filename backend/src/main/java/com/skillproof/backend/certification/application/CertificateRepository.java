package com.skillproof.backend.certification.application;

import com.skillproof.backend.certification.domain.Certificate;
import java.util.Optional;
import java.util.UUID;

public interface CertificateRepository {
    Certificate save(Certificate certificate);
    Optional<Certificate> findById(UUID id);
    Optional<Certificate> findByProgramAndLearner(UUID programId, UUID learnerId);
    Optional<Certificate> findBySerial(String serialNumber);
}
