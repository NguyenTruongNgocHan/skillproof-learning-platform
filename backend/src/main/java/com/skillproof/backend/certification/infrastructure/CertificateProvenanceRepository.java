package com.skillproof.backend.certification.infrastructure;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CertificateProvenanceRepository
    extends JpaRepository<CertificateProvenanceEntity, UUID> {}
