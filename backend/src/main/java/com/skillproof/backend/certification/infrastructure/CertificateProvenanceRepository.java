package com.skillproof.backend.certification.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CertificateProvenanceRepository
        extends JpaRepository<CertificateProvenanceEntity, UUID> {}
