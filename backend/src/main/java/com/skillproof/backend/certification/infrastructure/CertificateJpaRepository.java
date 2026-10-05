package com.skillproof.backend.certification.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.skillproof.backend.certification.domain.Certificate;

interface CertificateJpaRepository
    extends JpaRepository<CertificateEntity, UUID>
{
    Optional<CertificateEntity> findByCertificationProgramIdAndLearnerUserId(
        UUID programId,
        UUID learnerId
    );
    Optional<CertificateEntity> findBySerialNumber(String serialNumber);
    List<CertificateEntity> findByCertificationProgramIdInOrderByIssuedAtDesc(
        List<UUID> programIds
    );

    @Query(
        """
        select certificate from CertificateEntity certificate
        where certificate.certificationProgramId in :programIds
          and (:status is null or certificate.status = :status)
          and (:learnerId is null or certificate.learnerUserId = :learnerId)
          and (lower(certificate.serialNumber) like :pattern
            or lower(coalesce(certificate.learnerEmail, '')) like :pattern
            or lower(coalesce(certificate.programName, '')) like :pattern)
        order by certificate.issuedAt desc
        """
    )
    Page<CertificateEntity> search(
        List<UUID> programIds,
        Certificate.Status status,
        UUID learnerId,
        String pattern,
        Pageable pageable
    );
}
