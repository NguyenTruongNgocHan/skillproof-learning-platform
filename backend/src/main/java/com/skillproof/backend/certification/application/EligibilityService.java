package com.skillproof.backend.certification.application;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillproof.backend.certification.domain.CertificateEligibility;
import com.skillproof.backend.certification.domain.CertificationProgram;
import com.skillproof.backend.common.exception.BadRequestException;
import com.skillproof.backend.common.exception.ConflictException;
import com.skillproof.backend.common.exception.NotFoundException;
import com.skillproof.backend.course.contract.CertificationContextQuery;
import com.skillproof.backend.course.contract.CompletionEvidenceQuery;
import com.skillproof.backend.organization.contract.OrganizationAuthorityQuery;
import com.skillproof.backend.quiz.contract.QuizCompletionEvidenceQuery;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Service
public class EligibilityService {

    private static final String ISSUE_CERTIFICATES = "ISSUE_CERTIFICATES";

    private final com.skillproof.backend.access.contract.AccessEntitlementQuery entitlements;
    private final CertificationProgramRepository programRepository;
    private final CertificateEligibilityRepository eligibilityRepository;
    private final CertificationContextQuery certificationContextQuery;
    private final CompletionEvidenceQuery completionEvidenceQuery;
    private final OrganizationAuthorityQuery organizationAuthorityQuery;
    private final QuizCompletionEvidenceQuery quizCompletionEvidenceQuery;
    private final ObjectMapper objectMapper;

    public EligibilityService(
            CertificationProgramRepository programRepository,
            CertificateEligibilityRepository eligibilityRepository,
            CertificationContextQuery certificationContextQuery,
            CompletionEvidenceQuery completionEvidenceQuery,
            OrganizationAuthorityQuery organizationAuthorityQuery,
            QuizCompletionEvidenceQuery quizCompletionEvidenceQuery,
            ObjectMapper objectMapper, com.skillproof.backend.access.contract.AccessEntitlementQuery entitlements
    ) {
        this.programRepository = programRepository;
        this.eligibilityRepository = eligibilityRepository;
        this.certificationContextQuery = certificationContextQuery;
        this.completionEvidenceQuery = completionEvidenceQuery;
        this.organizationAuthorityQuery = organizationAuthorityQuery;
        this.quizCompletionEvidenceQuery = quizCompletionEvidenceQuery;
        this.objectMapper = objectMapper;
        this.entitlements = entitlements;
    }

    @Transactional
    public CertificateEligibility evaluate(
            UUID actorId,
            UUID programId,
            UUID enrollmentId
    ) {
        CertificationProgram program = requireProgram(programId);
        var assessmentEvidence = quizCompletionEvidenceQuery.evidence(
                program.courseVersionId(),
                enrollmentId
        );
        var evidence = completionEvidenceQuery.evaluate(
                enrollmentId,
                assessmentEvidence.requiredOfficialAssessments(),
                assessmentEvidence.passedOfficialAssessments()
        );

        boolean learnerSelf = actorId.equals(evidence.learnerId());
        boolean authorizedOrganizer = organizationAuthorityQuery.hasAuthority(
                program.organizationId(),
                actorId,
                ISSUE_CERTIFICATES
        );
        if (!learnerSelf && !authorizedOrganizer) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Eligibility can be evaluated by the learner or an authorized organizer"
            );
        }

        if (!program.courseVersionId().equals(evidence.courseVersionId())
                || !program.completionPolicyId().equals(evidence.completionPolicyId())) {
            throw new BadRequestException(
                    "ELIGIBILITY_VERSION_MISMATCH",
                    "Completion evidence does not belong to this certification program version and policy"
            );
        }

        entitlements.require(evidence.learnerId(), com.skillproof.backend.commerce.domain.ProductType.COURSE, evidence.courseVersionId());
        entitlements.require(evidence.learnerId(), com.skillproof.backend.commerce.domain.ProductType.CERTIFICATION, evidence.courseVersionId());
        var eligibility = new CertificateEligibility(
                UUID.randomUUID(),
                program.id(),
                evidence.learnerId(),
                enrollmentId,
                evidence.evaluationId(),
                evidence.completed()
                ? CertificateEligibility.Status.ELIGIBLE
                : CertificateEligibility.Status.NOT_ELIGIBLE,
                serializeEvidence(evidence),
                Instant.now()
        );
        return eligibilityRepository.save(eligibility);
    }

    @Transactional(readOnly = true)
    public CertificateEligibility latest(
            UUID actorId,
            UUID programId,
            UUID learnerId
    ) {
        CertificationProgram program = requireProgram(programId);
        boolean learnerSelf = actorId.equals(learnerId);
        boolean authorizedOrganizer = organizationAuthorityQuery.hasAuthority(
                program.organizationId(),
                actorId,
                ISSUE_CERTIFICATES
        );
        if (!learnerSelf && !authorizedOrganizer) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Eligibility is private"
            );
        }

        return eligibilityRepository
                .findLatest(programId, learnerId)
                .orElseThrow(()
                        -> new NotFoundException(
                        "ELIGIBILITY_NOT_FOUND",
                        "Eligibility evaluation not found"
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

    private String serializeEvidence(
            CompletionEvidenceQuery.Evidence evidence
    ) {
        try {
            return objectMapper.writeValueAsString(evidence);
        } catch (JacksonException exception) {
            throw new IllegalStateException(
                    "Cannot serialize completion evidence",
                    exception
            );
        }
    }
}
