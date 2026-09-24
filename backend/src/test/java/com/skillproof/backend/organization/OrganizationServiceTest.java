package com.skillproof.backend.organization;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.security.access.AccessDeniedException;

import com.skillproof.backend.common.exception.BadRequestException;
import com.skillproof.backend.common.exception.ConflictException;
import com.skillproof.backend.identity.domain.UserAccount;
import com.skillproof.backend.identity.domain.UserRole;
import com.skillproof.backend.identity.infrastructure.UserAccountRepository;
import com.skillproof.backend.organization.application.OrganizationService;
import com.skillproof.backend.organization.domain.Organization;
import com.skillproof.backend.organization.infrastructure.OrganizationRepository;

class OrganizationServiceTest {

    OrganizationRepository repository = mock(OrganizationRepository.class);
    UserAccountRepository users = mock(UserAccountRepository.class);
    OrganizationService service = new OrganizationService(repository, users);
    UUID actor = UUID.randomUUID(), orgId = UUID.randomUUID(), other = UUID.randomUUID();
    Organization org = new Organization(orgId, actor, "Example Ltd", "Example", null, "Education", "Vietnam", null, "Contact", "contact@example.org", null, Organization.Status.PENDING, null, Instant.now(), Instant.now());

    @BeforeEach
    void setup() {
        when(repository.find(orgId)).thenReturn(Optional.of(org));
        when(users.findById(actor)).thenReturn(Optional.of(UserAccount.newAdmin("admin@example.org", "hash", "Admin", Instant.now())));
    }

    @Test
    void reviewWritesDecisionAndAttribution() {
        when(repository.review(eq(orgId), eq(Organization.Status.APPROVED), any(), any())).thenReturn(1);
        service.review(orgId, actor, new OrganizationService.Review(Organization.Status.APPROVED, "Evidence checked"));
        verify(repository).logReview(eq(orgId), eq(actor), eq(Organization.Status.APPROVED), eq("Evidence checked"), any());
    }

    @Test
    void rejectionNeedsReason() {
        assertThrows(BadRequestException.class, () -> service.review(orgId, actor, new OrganizationService.Review(Organization.Status.REJECTED, " ")));
        verify(repository, never()).review(any(), any(), any(), any());
    }

    @Test
    void reviewCannotBeRepeated() {
        assertThrows(ConflictException.class, () -> service.review(orgId, actor, new OrganizationService.Review(Organization.Status.APPROVED, null)));
        verify(repository, never()).logReview(any(), any(), any(), any(), any());
    }

    @Test
    void crossOrganizationReadDenied() {
        assertThrows(AccessDeniedException.class, () -> service.get(orgId, other, false));
    }

    @Test
    void inactiveOrganizerCannotCreate() {
        var create = new OrganizationService.Create("Example", "Example", null, "Education", "Vietnam", null, "Contact", "contact@example.org", null);
        assertThrows(AccessDeniedException.class, () -> service.create(actor, create));
        verify(repository, never()).create(any());
    }

    @Test
    void rejectedApplicationCanBeResubmitted() {
        var rejected = new Organization(orgId, actor, "Example Ltd", "Example", null, "Education", "Vietnam", null, "Contact", "contact@example.org", null, Organization.Status.REJECTED, "Missing detail", Instant.now(), Instant.now());
        when(repository.owned(actor)).thenReturn(Optional.of(rejected));
        var organizer = UserAccount.newAccount("organizer@example.org", "hash", "Organizer", UserRole.ORGANIZER);
        organizer.verifyEmail(Instant.now());
        when(users.findById(actor)).thenReturn(Optional.of(organizer));
        when(repository.resubmit(eq(orgId), any(), any())).thenReturn(1);
        assertEquals(orgId, service.resubmit(actor, new OrganizationService.Create("Example Ltd", "Example", null, "Education", "Vietnam", null, "Contact", "contact@example.org", null)).id());
        verify(repository).resubmit(eq(orgId), any(), any());
    }

    @Test
    void grantRequiresApprovedOrganizationAndMembership() {
        when(users.findById(actor)).thenReturn(Optional.of(UserAccount.newAccount("o@example.org", "hash", "Organizer", UserRole.ORGANIZER)));
        assertThrows(AccessDeniedException.class, () -> service.update(orgId, actor, new OrganizationService.Update("Changed", null, "Education", null)));
    }
}
