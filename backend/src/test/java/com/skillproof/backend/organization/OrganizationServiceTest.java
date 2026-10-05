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
import com.skillproof.backend.identity.contract.IdentityAccessQuery;
import com.skillproof.backend.organization.application.OrganizationService;
import com.skillproof.backend.organization.domain.Organization;
import com.skillproof.backend.organization.infrastructure.OrganizationRepository;

class OrganizationServiceTest {

    private final OrganizationRepository repository = mock(
        OrganizationRepository.class
    );

    private final IdentityAccessQuery users = mock(IdentityAccessQuery.class);

    private final OrganizationService service = new OrganizationService(
        repository,
        users
    );

    private final UUID actor = UUID.randomUUID();
    private final UUID orgId = UUID.randomUUID();
    private final UUID other = UUID.randomUUID();

    private final Organization org = new Organization(
        orgId,
        actor,
        "Example Ltd",
        "Example",
        null,
        "Education",
        "Vietnam",
        null,
        "Contact",
        "contact@example.org",
        null,
        Organization.Status.PENDING,
        null,
        Instant.now(),
        Instant.now()
    );

    @BeforeEach
    void setup() {
        when(users.isActiveAdmin(actor)).thenReturn(true);
        var revision =
            new com.skillproof.backend.organization.infrastructure.OrganizationApplicationRevisionEntity(
                UUID.randomUUID(),
                orgId,
                1,
                "Example Ltd",
                "Example",
                null,
                "Education",
                "Vietnam",
                null,
                "Contact",
                "contact@example.org",
                null,
                Instant.now()
            );
        when(repository.latestRevision(orgId)).thenReturn(
            Optional.of(revision)
        );
        when(repository.applicationRevision(revision.getId())).thenReturn(
            Optional.of(revision)
        );

        when(repository.find(orgId)).thenReturn(Optional.of(org));

        when(users.find(actor)).thenReturn(
            Optional.of(account(actor, "admin@example.org", "ADMIN", true))
        );
    }

    @Test
    void reviewWritesDecisionAndAttribution() {
        when(
            repository.reviewRevision(
                any(),
                eq(actor),
                eq(Organization.Status.APPROVED),
                any(),
                any()
            )
        ).thenReturn(1);

        when(
            repository.review(
                eq(orgId),
                eq(Organization.Status.APPROVED),
                any(),
                any()
            )
        ).thenReturn(1);

        service.review(
            orgId,
            actor,
            new OrganizationService.Review(
                Organization.Status.APPROVED,
                "Evidence checked"
            )
        );

        verify(repository).logReview(
            eq(orgId),
            eq(actor),
            eq(Organization.Status.APPROVED),
            eq("Evidence checked"),
            any()
        );
    }

    @Test
    void rejectionNeedsReason() {
        assertThrows(BadRequestException.class, () ->
            service.review(
                orgId,
                actor,
                new OrganizationService.Review(
                    Organization.Status.REJECTED,
                    " "
                )
            )
        );

        verify(repository, never()).review(any(), any(), any(), any());
    }

    @Test
    void reviewCannotBeRepeated() {
        assertThrows(ConflictException.class, () ->
            service.review(
                orgId,
                actor,
                new OrganizationService.Review(
                    Organization.Status.APPROVED,
                    null
                )
            )
        );

        verify(repository, never()).logReview(
            any(),
            any(),
            any(),
            any(),
            any()
        );
    }

    @Test
    void crossOrganizationReadDenied() {
        assertThrows(AccessDeniedException.class, () ->
            service.get(orgId, other, false)
        );
    }

    @Test
    void inactiveOrganizerCannotCreate() {
        var create = new OrganizationService.Create(
            "Example",
            "Example",
            null,
            "Education",
            "Vietnam",
            null,
            "Contact",
            "contact@example.org",
            null
        );

        when(users.find(actor)).thenReturn(
            Optional.of(
                account(actor, "organizer@example.org", "ORGANIZER", false)
            )
        );

        assertThrows(AccessDeniedException.class, () ->
            service.create(actor, create)
        );

        verify(repository, never()).create(any());
    }

    @Test
    void rejectedApplicationCanBeResubmitted() {
        var rejected = new Organization(
            orgId,
            actor,
            "Example Ltd",
            "Example",
            null,
            "Education",
            "Vietnam",
            null,
            "Contact",
            "contact@example.org",
            null,
            Organization.Status.REJECTED,
            "Missing detail",
            Instant.now(),
            Instant.now()
        );

        when(repository.owned(actor)).thenReturn(Optional.of(rejected));

        when(users.find(actor)).thenReturn(
            Optional.of(
                account(actor, "organizer@example.org", "ORGANIZER", true)
            )
        );

        when(repository.resubmit(eq(orgId), any(), any())).thenReturn(1);

        when(repository.find(orgId)).thenReturn(
            Optional.of(
                new Organization(
                    orgId,
                    actor,
                    "Example Ltd",
                    "Example",
                    null,
                    "Education",
                    "Vietnam",
                    null,
                    "Contact",
                    "contact@example.org",
                    null,
                    Organization.Status.PENDING,
                    null,
                    rejected.createdAt(),
                    Instant.now()
                )
            )
        );

        assertEquals(
            orgId,
            service
                .resubmit(
                    actor,
                    new OrganizationService.Create(
                        "Example Ltd",
                        "Example",
                        null,
                        "Education",
                        "Vietnam",
                        null,
                        "Contact",
                        "contact@example.org",
                        null
                    )
                )
                .id()
        );

        verify(repository).resubmit(eq(orgId), any(), any());
    }

    @Test
    void grantRequiresApprovedOrganizationAndMembership() {
        when(users.find(actor)).thenReturn(
            Optional.of(account(actor, "o@example.org", "ORGANIZER", true))
        );

        assertThrows(AccessDeniedException.class, () ->
            service.update(
                orgId,
                actor,
                new OrganizationService.Update(
                    "Changed",
                    null,
                    "Education",
                    null
                )
            )
        );
    }

    @Test
    void addMemberByEmailRejectsExistingOrganizationMembership() {
        UUID memberId = UUID.randomUUID();

        when(users.find(actor)).thenReturn(
            Optional.of(
                account(actor, "organizer@example.org", "ORGANIZER", true)
            )
        );

        when(repository.hasGrant(orgId, actor, "MANAGE_MEMBERS")).thenReturn(
            true
        );

        when(users.findByEmail("member@example.org")).thenReturn(
            Optional.of(
                account(memberId, "member@example.org", "ORGANIZER", true)
            )
        );

        when(repository.memberOrganization(memberId)).thenReturn(
            Optional.of(org)
        );

        assertThrows(ConflictException.class, () ->
            service.addMember(orgId, actor, " MEMBER@EXAMPLE.ORG ")
        );

        verify(repository, never()).addMember(
            any(),
            any(),
            any(),
            any(),
            eq(false)
        );
    }

    @Test
    void inactiveAccountNeverReceivesOrganizationAuthority() {
        when(users.find(actor)).thenReturn(
            Optional.of(
                account(actor, "organizer@example.org", "ORGANIZER", false)
            )
        );

        assertEquals(false, service.can(orgId, actor, "MANAGE_MEMBERS"));

        verify(repository, never()).hasGrant(any(), any(), any());
    }

    @Test
    void creatorReceivesOwnerMembershipAndDraftState() {
        when(users.find(actor)).thenReturn(
            Optional.of(
                account(actor, "organizer@example.org", "ORGANIZER", true)
            )
        );
        var draft = service.create(
            actor,
            new OrganizationService.Create(
                "Example Ltd",
                "Example",
                null,
                "Education",
                "Vietnam",
                null,
                "Contact",
                "contact@example.org",
                null
            )
        );
        assertEquals(Organization.Status.DRAFT, draft.status());
        verify(repository).addMember(
            eq(draft.id()),
            eq(actor),
            eq(actor),
            any(),
            eq(true)
        );
    }

    @Test
    void reviewRequiresAnActiveAdministrator() {
        when(users.isActiveAdmin(other)).thenReturn(false);
        var id = repository.latestRevision(orgId).orElseThrow().getId();
        assertThrows(AccessDeniedException.class, () ->
            service.reviewRevision(
                id,
                other,
                new OrganizationService.Review(
                    Organization.Status.APPROVED,
                    null
                )
            )
        );
        verify(repository, never()).reviewRevision(
            any(),
            any(),
            any(),
            any(),
            any()
        );
    }

    @Test
    void draftIsNotAReviewDecision() {
        var id = repository.latestRevision(orgId).orElseThrow().getId();
        assertThrows(BadRequestException.class, () ->
            service.reviewRevision(
                id,
                actor,
                new OrganizationService.Review(Organization.Status.DRAFT, null)
            )
        );
        verify(repository, never()).reviewRevision(
            any(),
            any(),
            any(),
            any(),
            any()
        );
    }

    private IdentityAccessQuery.Account account(
        UUID id,
        String email,
        String role,
        boolean active
    ) {
        return new IdentityAccessQuery.Account(
            id,
            email,
            role,
            active ? "ACTIVE" : "INACTIVE"
        );
    }
}
