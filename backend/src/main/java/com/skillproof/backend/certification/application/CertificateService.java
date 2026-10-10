package com.skillproof.backend.certification.application;

import com.skillproof.backend.certification.domain.Certificate;
import com.skillproof.backend.certification.domain.CertificateEligibility;
import com.skillproof.backend.certification.domain.CertificationProgram;
import com.skillproof.backend.common.exception.BadRequestException;
import com.skillproof.backend.common.exception.ConflictException;
import com.skillproof.backend.common.exception.NotFoundException;
import com.skillproof.backend.organization.contract.OrganizationAuthorityQuery;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CertificateService {

    private static final String ISSUE_CERTIFICATES = "ISSUE_CERTIFICATES";
    private final CertificationProgramRepository programRepository;
    private final OrganizationAuthorityQuery organizationAuthorityQuery;
    private final CertificateEligibilityRepository eligibilityRepository;
    private final com.skillproof.backend.access.contract.AccessEntitlementQuery entitlements;
    private final com.skillproof.backend.course.contract.CompletionEvidenceQuery completion;
    private final CertificateRepository certificateRepository;
    private final com.skillproof.backend.organization.contract.OrganizationPublicQuery issuerQuery;
    private final com.skillproof.backend.identity.contract.IdentityAccessQuery learnerQuery;

    public CertificateService(
            CertificationProgramRepository programRepository,
            OrganizationAuthorityQuery organizationAuthorityQuery,
            CertificateEligibilityRepository eligibilityRepository,
            CertificateRepository certificateRepository,
            com.skillproof.backend.organization.contract.OrganizationPublicQuery issuerQuery,
            com.skillproof.backend.identity.contract.IdentityAccessQuery learnerQuery,
             com.skillproof.backend.access.contract.AccessEntitlementQuery entitlements,
            com.skillproof.backend.course.contract.CompletionEvidenceQuery completion
    ) {
        this.programRepository = programRepository;
        this.organizationAuthorityQuery = organizationAuthorityQuery;
        this.eligibilityRepository = eligibilityRepository;
        this.certificateRepository = certificateRepository;
        this.entitlements = entitlements;
        this.completion = completion;
        this.issuerQuery = issuerQuery;
        this.learnerQuery = learnerQuery;
    }

    @Transactional(readOnly = true)
    public List<Certificate> listCertificates(
            UUID actorId,
            UUID organizationId
    ) {
        requireCertificateAuthority(organizationId, actorId);
        var programIds = programRepository
                .findByOrganizationId(organizationId)
                .stream()
                .map(CertificationProgram::id)
                .toList();
        return certificateRepository.findByProgramIds(programIds);
    }

    @Transactional(readOnly = true)
    public Page<Certificate> searchCertificates(
            UUID actorId,
            UUID organizationId,
            Certificate.Status status,
            UUID learnerId,
            String query,
            int page,
            int size
    ) {
        requireCertificateAuthority(organizationId, actorId);
        var programIds = programRepository
                .findByOrganizationId(organizationId)
                .stream()
                .map(CertificationProgram::id)
                .toList();
        if (programIds.isEmpty()) {
            return Page.empty();
        }
        return certificateRepository.searchByProgramIds(
                programIds,
                status,
                learnerId,
                "%"
                + (query == null
                        ? ""
                        : query.trim().toLowerCase(java.util.Locale.ROOT))
                + "%",
                PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100))
        );
    }

    @Transactional(readOnly = true)
    public Certificate certificate(UUID actorId, UUID certificateId) {
        Certificate certificate = certificateRepository
                .findById(certificateId)
                .orElseThrow(()
                        -> new NotFoundException(
                        "CERTIFICATE_NOT_FOUND",
                        "Certificate not found"
                )
                );
        CertificationProgram program = requireProgram(
                certificate.certificationProgramId()
        );
        requireCertificateAuthority(program.organizationId(), actorId);
        return certificate;
    }

    @Transactional
    public Certificate issue(UUID actorId, UUID eligibilityId) {
        var eligibility = eligibilityRepository
                .findById(eligibilityId)
                .orElseThrow(()
                        -> new NotFoundException(
                        "ELIGIBILITY_NOT_FOUND",
                        "Eligibility evaluation not found"
                )
                );
        var program = programRepository
                .findForIssue(eligibility.certificationProgramId())
                .orElseThrow(()
                        -> new NotFoundException(
                        "CERTIFICATION_PROGRAM_NOT_FOUND",
                        "Certification program not found"
                )
                );
        requireCertificateAuthority(program.organizationId(), actorId);
        if (eligibility.status() != CertificateEligibility.Status.ELIGIBLE) {
            throw new ConflictException(
                    "LEARNER_NOT_ELIGIBLE",
                    "Only eligible learners can receive a certificate"
            );
        }
        var existing = certificateRepository.findByProgramAndLearner(
                program.id(),
                eligibility.learnerUserId()
        );
        if (existing.isPresent()) {
            return existing.get();
        }
        if (program.status() != CertificationProgram.Status.ACTIVE) {
            throw new ConflictException(
                    "CERTIFICATION_PROGRAM_NOT_ACTIVE",
                    "Certification program is not active"
            );
        }

        issuerQuery.requireApprovedForUpdate(program.organizationId());
        entitlements.require(eligibility.learnerUserId(), com.skillproof.backend.commerce.domain.ProductType.COURSE, program.courseVersionId());
        entitlements.require(eligibility.learnerUserId(), com.skillproof.backend.commerce.domain.ProductType.CERTIFICATION, program.courseVersionId());
        var current = completion.evaluate(eligibility.enrollmentId(), 0, 0);
        if (!current.completed() || !current.courseVersionId().equals(program.courseVersionId())
                || !current.learnerId().equals(eligibility.learnerUserId())) {
            throw new ConflictException("ELIGIBILITY_STALE", "Fresh completion evidence is required");
        }
        // Every issuer locks the same program row before checking this invariant.
        // Do not recover a unique violation inside a rollback-only transaction.
        return certificateRepository.save(
                new Certificate(
                        UUID.randomUUID(),
                        program.id(),
                        eligibility.id(),
                        eligibility.learnerUserId(),
                        "SP-"
                        + UUID.randomUUID()
                                .toString()
                                .replace("-", "")
                                .substring(0, 20)
                                .toUpperCase(),
                        Certificate.Status.ISSUED,
                        Instant.now(),
                        null,
                        null,
                        program.organizationId(),
                        program.name(),
                        program.courseVersionId(),
                        issuerQuery
                                .find(program.organizationId())
                                .orElseThrow()
                                .displayName(),
                        learnerQuery
                                .find(eligibility.learnerUserId())
                                .orElseThrow()
                                .email()
                )
        );
    }

    @Transactional
    public Certificate revoke(UUID actorId, UUID certificateId, String reason) {
        if (reason == null || reason.isBlank() || reason.length() > 1000) {
            throw new BadRequestException(
                    "REVOCATION_REASON_REQUIRED",
                    "Provide a reason of 1 to 1000 characters"
            );
        }
        var existing = certificateRepository
                .findById(certificateId)
                .orElseThrow(()
                        -> new NotFoundException(
                        "CERTIFICATE_NOT_FOUND",
                        "Certificate not found"
                )
                );
        var program = programRepository
                .findForIssue(existing.certificationProgramId())
                .orElseThrow(()
                        -> new NotFoundException(
                        "CERTIFICATION_PROGRAM_NOT_FOUND",
                        "Certification program not found"
                )
                );
        existing = certificateRepository.findById(certificateId).orElseThrow();
        requireCertificateAuthority(program.organizationId(), actorId);
        if (existing.status() == Certificate.Status.REVOKED) {
            return existing;
        }
        return certificateRepository.save(
                new Certificate(
                        existing.id(),
                        existing.certificationProgramId(),
                        existing.eligibilityId(),
                        existing.learnerUserId(),
                        existing.serialNumber(),
                        Certificate.Status.REVOKED,
                        existing.issuedAt(),
                        Instant.now(),
                        reason,
                        existing.organizationId(),
                        existing.programName(),
                        existing.courseVersionId(),
                        existing.issuerName(),
                        existing.learnerEmail()
                )
        );
    }

    @Transactional(readOnly = true)
    public Certificate verify(String serialNumber) {
        return certificateRepository
                .findBySerial(serialNumber)
                .orElseThrow(()
                        -> new NotFoundException(
                        "CERTIFICATE_NOT_FOUND",
                        "Certificate not found"
                )
                );
    }

    private CertificationProgram requireProgram(UUID programId) {
        CertificationProgram program = programRepository
                .findById(programId)
                .orElseThrow(()
                        -> new NotFoundException(
                        "CERTIFICATION_PROGRAM_NOT_FOUND",
                        "Certification program not found"
                )
                );
        return program;
    }

    private void requireCertificateAuthority(
            UUID organizationId,
            UUID actorId
    ) {
        if (!organizationAuthorityQuery.hasAuthority(
                organizationId,
                actorId,
                ISSUE_CERTIFICATES
        )) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Certificate authority is required"
            );
        }
    }

    @Transactional(readOnly = true)
    public List<Certificate> mine(UUID learner) {
        if (!learnerQuery.isActiveLearner(learner)) {
            throw new org.springframework.security.access.AccessDeniedException("Active learner required");
        }
        return certificateRepository.findByLearner(learner);
    }

}
