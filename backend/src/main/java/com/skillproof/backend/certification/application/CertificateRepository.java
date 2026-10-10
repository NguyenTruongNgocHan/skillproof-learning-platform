package com.skillproof.backend.certification.application;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.skillproof.backend.certification.domain.Certificate;

public interface CertificateRepository {

    List<Certificate> findByLearner(UUID learner);

    Certificate save(Certificate certificate);

    Optional<Certificate> findById(UUID id);

    Optional<Certificate> findByProgramAndLearner(
            UUID programId,
            UUID learnerId
    );

    Optional<Certificate> findBySerial(String serialNumber);

    List<Certificate> findByProgramIds(List<UUID> programIds);

    Page<Certificate> searchByProgramIds(
            List<UUID> programIds,
            Certificate.Status status,
            UUID learnerId,
            String pattern,
            Pageable pageable
    );
}
