package com.skillproof.backend.certification;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.security.access.AccessDeniedException;

import com.skillproof.backend.certification.application.CertificationProgramRepository;
import com.skillproof.backend.certification.domain.CertificationProgram;
import com.skillproof.backend.learning.contract.CertificationContextQuery;
import com.skillproof.backend.learning.contract.EnrollmentLookupQuery;
import com.skillproof.backend.organization.contract.OrganizationAuthorityQuery;

class EligibilityServiceTest {

    private final CertificationProgramRepository programs = mock(
        CertificationProgramRepository.class
    );
    private final OrganizationAuthorityQuery authority = mock(
        OrganizationAuthorityQuery.class
    );
    private final com.skillproof.backend.certification.application.CertificationProgramService service =
        new com.skillproof.backend.certification.application.CertificationProgramService(
            programs,
            authority,
            mock(CertificationContextQuery.class),
            mock(EnrollmentLookupQuery.class)
        );

    private final UUID actorId = UUID.randomUUID();
    private final UUID organizationId = UUID.randomUUID();

    @Test
    void listsProgramsForAuthorizedOrganization() {
        var program = new CertificationProgram(
            UUID.randomUUID(),
            organizationId,
            UUID.randomUUID(),
            UUID.randomUUID(),
            "Program",
            CertificationProgram.Status.ACTIVE,
            actorId,
            Instant.now()
        );
        when(
            authority.hasAuthority(
                organizationId,
                actorId,
                "ISSUE_CERTIFICATES"
            )
        ).thenReturn(true);
        when(programs.findByOrganizationId(organizationId)).thenReturn(
            List.of(program)
        );

        assertEquals(
            List.of(program),
            service.listPrograms(actorId, organizationId)
        );
        verify(programs).findByOrganizationId(organizationId);
    }

    @Test
    void doesNotListProgramsWithoutCertificateAuthority() {
        when(
            authority.hasAuthority(
                organizationId,
                actorId,
                "ISSUE_CERTIFICATES"
            )
        ).thenReturn(false);

        assertThrows(AccessDeniedException.class, () ->
            service.listPrograms(actorId, organizationId)
        );
    }
}
