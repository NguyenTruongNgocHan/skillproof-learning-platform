package com.skillproof.backend.certification.application;

import com.skillproof.backend.certification.domain.Certificate;
import com.skillproof.backend.certification.domain.CertificationProgram;
import com.skillproof.backend.common.exception.BadRequestException;
import com.skillproof.backend.common.exception.ConflictException;
import com.skillproof.backend.common.exception.NotFoundException;
import com.skillproof.backend.course.contract.CertificationContextQuery;
import com.skillproof.backend.course.contract.EnrollmentLookupQuery;
import com.skillproof.backend.organization.contract.OrganizationAuthorityQuery;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CertificationProgramService {

    private static final String ISSUE_CERTIFICATES = "ISSUE_CERTIFICATES";
    private final CertificationProgramRepository programRepository;
    private final OrganizationAuthorityQuery organizationAuthorityQuery;
    private final CertificationContextQuery certificationContextQuery;
    private final EnrollmentLookupQuery enrollmentLookupQuery;

    public CertificationProgramService(
            CertificationProgramRepository programRepository,
            OrganizationAuthorityQuery organizationAuthorityQuery,
            CertificationContextQuery certificationContextQuery,
            EnrollmentLookupQuery enrollmentLookupQuery
    ) {
        this.programRepository = programRepository;
        this.organizationAuthorityQuery = organizationAuthorityQuery;
        this.certificationContextQuery = certificationContextQuery;
        this.enrollmentLookupQuery = enrollmentLookupQuery;
    }

    @Transactional
    public CertificationProgram createProgram(
            UUID actorId,
            UUID organizationId,
            UUID courseVersionId,
            String name
    ) {
        requireCertificateAuthority(organizationId, actorId);

        var context = certificationContextQuery.requireCertificationContext(
                courseVersionId
        );
        if (!organizationId.equals(context.organizationId())) {
            throw new BadRequestException(
                    "PROGRAM_ORG_MISMATCH",
                    "Certification program organization must own the course"
            );
        }
        if (!context.published()) {
            throw new ConflictException(
                    "COURSE_VERSION_NOT_PUBLISHED",
                    "Certification program requires a published course version"
            );
        }

        var program = new CertificationProgram(
                UUID.randomUUID(),
                organizationId,
                courseVersionId,
                context.completionPolicyId(),
                name.trim(),
                CertificationProgram.Status.ACTIVE,
                actorId,
                Instant.now()
        );
        return programRepository.save(program);
    }

    @Transactional(readOnly = true)
    public List<CertificationProgram> listPrograms(
            UUID actorId,
            UUID organizationId
    ) {
        requireCertificateAuthority(organizationId, actorId);
        return programRepository.findByOrganizationId(organizationId);
    }

    @Transactional(readOnly = true)
    public Page<EnrollmentLookupQuery.EnrollmentSummary> searchEnrollments(
            UUID actorId,
            UUID programId,
            String query,
            int page,
            int size
    ) {
        CertificationProgram program = requireProgram(programId);
        requireCertificateAuthority(program.organizationId(), actorId);
        return enrollmentLookupQuery.search(
                program.organizationId(),
                program.courseVersionId(),
                query,
                page,
                size
        );
    }

    @Transactional(readOnly = true)
    public java.util.List<CertificationContextQuery.PublishedSource> sources(
            UUID actor,
            UUID organizationId
    ) {
        requireCertificateAuthority(organizationId, actor);
        return certificationContextQuery.publishedSources(organizationId);
    }

    @Transactional
    public CertificationProgram retire(UUID actor, UUID id) {
        var program = programRepository
                .findForIssue(id)
                .orElseThrow(()
                        -> new NotFoundException(
                        "CERTIFICATION_PROGRAM_NOT_FOUND",
                        "Program not found"
                )
                );
        requireCertificateAuthority(program.organizationId(), actor);
        if (program.status() == CertificationProgram.Status.RETIRED) {
            return program;
        }
        return programRepository.save(
                new CertificationProgram(
                        program.id(),
                        program.organizationId(),
                        program.courseVersionId(),
                        program.completionPolicyId(),
                        program.name(),
                        CertificationProgram.Status.RETIRED,
                        program.createdByUserId(),
                        program.createdAt()
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
        if (program.status() != CertificationProgram.Status.ACTIVE) {
            throw new ConflictException(
                    "CERTIFICATION_PROGRAM_NOT_ACTIVE",
                    "Certification program is not active"
            );
        }
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
}
