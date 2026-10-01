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
import com.skillproof.backend.common.exception.UnauthorizedException;
import com.skillproof.backend.learning.contract.CertificationContextQuery;
import com.skillproof.backend.learning.contract.CompletionEvidenceQuery;
import com.skillproof.backend.organization.contract.OrganizationAuthorityQuery;
import com.skillproof.backend.quiz.contract.QuizCompletionEvidenceQuery;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Service
public class EligibilityService {

    private static final String ISSUE_CERTIFICATES = "ISSUE_CERTIFICATES";

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
            ObjectMapper objectMapper) {
        this.programRepository = programRepository;
        this.eligibilityRepository = eligibilityRepository;
        this.certificationContextQuery = certificationContextQuery;
        this.completionEvidenceQuery = completionEvidenceQuery;
        this.organizationAuthorityQuery = organizationAuthorityQuery;
        this.quizCompletionEvidenceQuery = quizCompletionEvidenceQuery;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public CertificationProgram createProgram(
            UUID actorId,
            UUID organizationId,
            UUID learningPathVersionId,
            String name) {
        requireCertificateAuthority(organizationId, actorId);

        var context = certificationContextQuery.requireCertificationContext(learningPathVersionId);
        if (!organizationId.equals(context.organizationId())) {
            throw new BadRequestException(
                    "PROGRAM_ORG_MISMATCH",
                    "Certification program organization must own the learning path");
        }
        if (!context.published()) {
            throw new ConflictException(
                    "LPV_NOT_PUBLISHED",
                    "Certification program requires a published learning path version");
        }

        var program = new CertificationProgram(
                UUID.randomUUID(),
                organizationId,
                learningPathVersionId,
                context.completionPolicyId(),
                name,
                CertificationProgram.Status.ACTIVE,
                actorId,
                Instant.now());
        return programRepository.save(program);
    }

    @Transactional
    public CertificateEligibility evaluate(UUID actorId, UUID programId, UUID enrollmentId) {
        CertificationProgram program = requireProgram(programId);
        var assessmentEvidence = quizCompletionEvidenceQuery.evidence(
                program.learningPathVersionId(), enrollmentId);
        var evidence = completionEvidenceQuery.evaluate(
                enrollmentId,
                assessmentEvidence.requiredOfficialAssessments(),
                assessmentEvidence.passedOfficialAssessments());

        boolean learnerSelf = actorId.equals(evidence.learnerId());
        boolean authorizedOrganizer = organizationAuthorityQuery.hasAuthority(
                program.organizationId(), actorId, ISSUE_CERTIFICATES);
        if (!learnerSelf && !authorizedOrganizer) {
            throw new UnauthorizedException(
                    "ELIGIBILITY_FORBIDDEN",
                    "Eligibility can be evaluated by the learner or an authorized organizer");
        }

        if (!program.learningPathVersionId().equals(evidence.pathVersionId())
                || !program.completionPolicyId().equals(evidence.completionPolicyId())) {
            throw new BadRequestException(
                    "ELIGIBILITY_VERSION_MISMATCH",
                    "Completion evidence does not belong to this certification program version and policy");
        }

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
                Instant.now());
        return eligibilityRepository.save(eligibility);
    }

    @Transactional(readOnly = true)
    public CertificateEligibility latest(UUID actorId, UUID programId, UUID learnerId) {
        CertificationProgram program = requireProgram(programId);
        boolean learnerSelf = actorId.equals(learnerId);
        boolean authorizedOrganizer = organizationAuthorityQuery.hasAuthority(
                program.organizationId(), actorId, ISSUE_CERTIFICATES);
        if (!learnerSelf && !authorizedOrganizer) {
            throw new UnauthorizedException("ELIGIBILITY_FORBIDDEN", "Eligibility is private");
        }
        return eligibilityRepository.findLatest(programId, learnerId)
                .orElseThrow(() -> new NotFoundException(
                "ELIGIBILITY_NOT_FOUND", "Eligibility evaluation not found"));
    }

    private CertificationProgram requireProgram(UUID programId) {
        CertificationProgram program = programRepository.findById(programId)
                .orElseThrow(() -> new NotFoundException(
                "CERTIFICATION_PROGRAM_NOT_FOUND", "Certification program not found"));
        if (program.status() != CertificationProgram.Status.ACTIVE) {
            throw new ConflictException(
                    "CERTIFICATION_PROGRAM_NOT_ACTIVE", "Certification program is not active");
        }
        return program;
    }

    private void requireCertificateAuthority(UUID organizationId, UUID actorId) {
        if (!organizationAuthorityQuery.hasAuthority(organizationId, actorId, ISSUE_CERTIFICATES)) {
            throw new UnauthorizedException(
                    "ORG_AUTHORITY_REQUIRED", "Certificate authority is required");
        }
    }

    private String serializeEvidence(CompletionEvidenceQuery.Evidence evidence) {
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
