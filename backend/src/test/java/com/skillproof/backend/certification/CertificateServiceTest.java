package com.skillproof.backend.certification;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.skillproof.backend.certification.application.*;
import com.skillproof.backend.certification.domain.*;
import com.skillproof.backend.common.exception.ConflictException;
import com.skillproof.backend.identity.contract.IdentityAccessQuery;
import com.skillproof.backend.organization.contract.*;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

class CertificateServiceTest {

    private final CertificationProgramRepository programs = mock(
        CertificationProgramRepository.class
    );
    private final CertificateEligibilityRepository eligibility = mock(
        CertificateEligibilityRepository.class
    );
    private final CertificateRepository certificates = mock(
        CertificateRepository.class
    );
    private final OrganizationAuthorityQuery authority = mock(
        OrganizationAuthorityQuery.class
    );
    private final OrganizationPublicQuery issuers = mock(
        OrganizationPublicQuery.class
    );
    private final IdentityAccessQuery users = mock(IdentityAccessQuery.class);
    private final CertificateService service = new CertificateService(
        programs,
        authority,
        eligibility,
        certificates,
        issuers,
        users
    );
    private final UUID actor = UUID.randomUUID(),
        organization = UUID.randomUUID(),
        learner = UUID.randomUUID();
    private final CertificationProgram program = new CertificationProgram(
        UUID.randomUUID(),
        organization,
        UUID.randomUUID(),
        UUID.randomUUID(),
        "Web foundations",
        CertificationProgram.Status.ACTIVE,
        actor,
        Instant.now()
    );

    private CertificateEligibility setup(CertificateEligibility.Status status) {
        var value = new CertificateEligibility(
            UUID.randomUUID(),
            program.id(),
            learner,
            UUID.randomUUID(),
            UUID.randomUUID(),
            status,
            "{}",
            Instant.now()
        );
        when(eligibility.findById(value.id())).thenReturn(Optional.of(value));
        when(programs.findForIssue(program.id())).thenReturn(
            Optional.of(program)
        );
        when(
            authority.hasAuthority(organization, actor, "ISSUE_CERTIFICATES")
        ).thenReturn(true);
        return value;
    }

    @Test
    void issuanceReturnsExistingCertificateWithoutAnotherInsert() {
        var evaluation = setup(CertificateEligibility.Status.ELIGIBLE);
        var existing = new Certificate(
            UUID.randomUUID(),
            program.id(),
            evaluation.id(),
            learner,
            "SP-EXISTING",
            Certificate.Status.ISSUED,
            Instant.now(),
            null,
            null
        );
        when(
            certificates.findByProgramAndLearner(program.id(), learner)
        ).thenReturn(Optional.of(existing));
        assertSame(existing, service.issue(actor, evaluation.id()));
        verify(programs).findForIssue(program.id());
        verify(certificates, never()).save(any());
    }

    @Test
    void ineligibleLearnerCannotReceiveCertificate() {
        var evaluation = setup(CertificateEligibility.Status.NOT_ELIGIBLE);
        assertThrows(ConflictException.class, () ->
            service.issue(actor, evaluation.id())
        );
        verify(certificates, never()).save(any());
    }

    @Test
    void organizationAuthorityIsRequiredBeforeIssuance() {
        var evaluation = setup(CertificateEligibility.Status.ELIGIBLE);
        when(
            authority.hasAuthority(organization, actor, "ISSUE_CERTIFICATES")
        ).thenReturn(false);
        assertThrows(AccessDeniedException.class, () ->
            service.issue(actor, evaluation.id())
        );
        verifyNoInteractions(certificates);
    }

    @Test
    void newCertificateCapturesIssuerAndLearnerSnapshots() {
        var evaluation = setup(CertificateEligibility.Status.ELIGIBLE);
        when(issuers.find(organization)).thenReturn(
            Optional.of(
                new OrganizationPublicQuery.OrganizationView(
                    organization,
                    "Example Academy",
                    "APPROVED"
                )
            )
        );
        when(users.find(learner)).thenReturn(
            Optional.of(
                new IdentityAccessQuery.Account(
                    learner,
                    "learner@example.org",
                    "LEARNER",
                    "ACTIVE"
                )
            )
        );
        when(certificates.save(any())).thenAnswer(call -> call.getArgument(0));
        var certificate = service.issue(actor, evaluation.id());
        assertEquals("Example Academy", certificate.issuerName());
        assertEquals("learner@example.org", certificate.learnerEmail());
        assertEquals(
            program.learningPathVersionId(),
            certificate.learningPathVersionId()
        );
        assertEquals(organization, certificate.organizationId());
    }
}
