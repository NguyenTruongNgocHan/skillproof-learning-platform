package com.skillproof.backend.organization;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.access.AccessDeniedException;

import com.skillproof.backend.common.exception.ConflictException;
import com.skillproof.backend.identity.contract.IdentityAccessQuery;
import com.skillproof.backend.organization.application.OrganizationInvitationService;
import com.skillproof.backend.organization.domain.Organization;
import com.skillproof.backend.organization.infrastructure.OrganizationInvitationEntity;
import com.skillproof.backend.organization.infrastructure.OrganizationInvitationJpaRepository;
import com.skillproof.backend.organization.infrastructure.OrganizationRepository;

class OrganizationInvitationServiceTest {

    private final OrganizationInvitationJpaRepository invitations = mock(
        OrganizationInvitationJpaRepository.class
    );
    private final OrganizationRepository organizations = mock(
        OrganizationRepository.class
    );
    private final IdentityAccessQuery users = mock(IdentityAccessQuery.class);
    private final OrganizationInvitationService service =
        new OrganizationInvitationService(
            invitations,
            organizations,
            users,
            mock(JavaMailSender.class)
        );
    private final UUID actor = UUID.randomUUID();
    private final UUID organization = UUID.randomUUID();

    private OrganizationInvitationEntity setup(Instant expiry, String email) {
        var invitation = new OrganizationInvitationEntity(
            UUID.randomUUID(),
            organization,
            email,
            "hash",
            UUID.randomUUID(),
            expiry,
            Instant.now()
        );
        when(invitations.findByTokenHashForUpdate(anyString())).thenReturn(
            Optional.of(invitation)
        );
        when(users.find(actor)).thenReturn(
            Optional.of(
                new IdentityAccessQuery.Account(
                    actor,
                    "organizer@example.com",
                    "ORGANIZER",
                    "ACTIVE"
                )
            )
        );
        when(organizations.lockApplication(organization)).thenReturn(Optional.of(
            new Organization(
                organization,
                UUID.randomUUID(),
                "Legal name",
                "Organization",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                Organization.Status.APPROVED,
                null,
                Instant.now(),
                Instant.now()
            )
        ));
        return invitation;
    }

    @Test
    void repeatedAcceptIsIdempotent() {
        var invitation = setup(
            Instant.now().plusSeconds(3600),
            "organizer@example.com"
        );
        invitation.accept(actor, Instant.now());
        assertDoesNotThrow(() -> service.accept(actor, "token"));
        verify(organizations, never()).addMember(
            any(),
            any(),
            any(),
            any(),
            anyBoolean()
        );
    }

    @Test
    void recipientMismatchCannotAcceptEvenAcceptedToken() {
        var invitation = setup(
            Instant.now().plusSeconds(3600),
            "another@example.com"
        );
        invitation.accept(UUID.randomUUID(), Instant.now());
        assertThrows(AccessDeniedException.class, () ->
            service.accept(actor, "token")
        );
        verifyNoInteractions(organizations);
    }

    @Test
    void expiredTokenCannotCreateMembership() {
        setup(Instant.now().minusSeconds(1), "organizer@example.com");
        assertThrows(ConflictException.class, () ->
            service.accept(actor, "token")
        );
        verifyNoInteractions(organizations);
    }

    @Test
    void revokedTokenCannotCreateMembership() {
        var invitation = setup(
            Instant.now().plusSeconds(3600),
            "organizer@example.com"
        );
        invitation.revoke();
        assertThrows(ConflictException.class, () ->
            service.accept(actor, "token")
        );
        verifyNoInteractions(organizations);
    }

    @Test
    void explicitAcceptCreatesOneMembership() {
        var invitation = setup(
            Instant.now().plusSeconds(3600),
            "organizer@example.com"
        );
        service.accept(actor, "token");
        service.accept(actor, "token");
        assertEquals("ACCEPTED", invitation.getStatus());
        verify(organizations, times(1)).addMember(
            eq(organization),
            eq(actor),
            eq(actor),
            any(),
            eq(false)
        );
    }
}
