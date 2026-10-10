package com.skillproof.backend.organization.application;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillproof.backend.common.exception.BadRequestException;
import com.skillproof.backend.common.exception.ConflictException;
import com.skillproof.backend.common.exception.NotFoundException;
import com.skillproof.backend.identity.contract.IdentityAccessQuery;
import com.skillproof.backend.organization.contract.OrganizationDocumentQuery;
import com.skillproof.backend.organization.domain.ApplicationRevisionView;
import com.skillproof.backend.organization.domain.Organization;
import com.skillproof.backend.organization.domain.OrganizationMemberView;
import com.skillproof.backend.organization.domain.OrganizationReviewView;
import com.skillproof.backend.organization.infrastructure.OrganizationRepository;

@Service
public class OrganizationService {

    private final OrganizationRepository organizations;

    private final IdentityAccessQuery users;
    private final OrganizationDocumentQuery documents;

    @Autowired
    public OrganizationService(
            OrganizationRepository organizations,
            IdentityAccessQuery users,
            OrganizationDocumentQuery documents
    ) {
        this.organizations = organizations;
        this.users = users;
        this.documents = documents;
    }

    public OrganizationService(
            OrganizationRepository organizations,
            IdentityAccessQuery users
    ) {
        this(organizations, users, organizationId -> List.of());
    }

    private void organizer(UUID actor) {
        var u = users
                .find(actor)
                .orElseThrow(()
                        -> new AccessDeniedException("Account unavailable")
                );
        if (!u.active() || !"ORGANIZER".equals(u.role())) {
            throw new AccessDeniedException("Active organizer required");
        }
    }

    private Organization find(UUID id) {
        return organizations
                .find(id)
                .orElseThrow(()
                        -> new NotFoundException(
                        "ORGANIZATION_NOT_FOUND",
                        "Organization not found"
                )
                );
    }

    private void allow(UUID id, UUID actor, String authority) {
        organizer(actor);
        if (!organizations.hasGrant(id, actor, authority)) {
            throw new AccessDeniedException("Organization authority required");
        }
    }

    @Transactional
    public Organization create(UUID actor, Create input) {
        organizer(actor);
        if (organizations.owned(actor).isPresent()) {
            throw new ConflictException(
                    "ORGANIZATION_ALREADY_EXISTS",
                    "This organizer already has an organization application"
            );
        }
        Instant now = Instant.now();
        var o = new Organization(
                UUID.randomUUID(),
                actor,
                input.legalName().trim(),
                input.displayName().trim(),
                input.website(),
                input.industry().trim(),
                input.country().trim(),
                input.registrationNumber(),
                input.contactName().trim(),
                input.contactEmail().trim(),
                input.contactPhone(),
                Organization.Status.DRAFT,
                null,
                now,
                now
        );
        try {
            organizations.create(o);
            organizations.addMember(o.id(), actor, actor, now, true);
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException(
                    "ORGANIZATION_ALREADY_EXISTS",
                    "An organization application already exists"
            );
        }
        return o;
    }

    @Transactional
    public Organization saveDraft(UUID actor, Create input) {
        organizer(actor);
        Organization previous = organizations
                .owned(actor)
                .orElseThrow(()
                        -> new NotFoundException(
                        "ORGANIZATION_NOT_FOUND",
                        "No organization draft found"
                )
                );
        Organization replacement = new Organization(
                previous.id(),
                actor,
                input.legalName().trim(),
                input.displayName().trim(),
                input.website(),
                input.industry().trim(),
                input.country().trim(),
                input.registrationNumber(),
                input.contactName().trim(),
                input.contactEmail().trim(),
                input.contactPhone(),
                Organization.Status.DRAFT,
                null,
                previous.createdAt(),
                Instant.now()
        );
        if (organizations.saveDraft(
                previous.id(),
                replacement,
                Instant.now()
        ) != 1) {
            throw new ConflictException(
                    "DRAFT_STATE_INVALID",
                    "Only a draft application can be saved"
            );
        }
        return find(previous.id());
    }

    @Transactional
    public Organization submit(UUID actor) {
        organizer(actor);
        Organization draft = organizations
                .owned(actor)
                .orElseThrow(()
                        -> new NotFoundException(
                        "ORGANIZATION_NOT_FOUND",
                        "No organization draft found"
                )
                );
        Instant now = Instant.now();
        if (organizations.submitDraft(draft.id(), now) != 1) {
            throw new ConflictException(
                    "DRAFT_STATE_INVALID",
                    "Only a draft application can be submitted"
            );
        }
        Organization submitted = find(draft.id());
        organizations.createRevision(
                submitted,
                documents.documentIds(submitted.id()),
                now
        );
        return submitted;
    }

    @Transactional
    public Organization resubmit(UUID actor, Create input) {
        organizer(actor);
        Organization previous = organizations
                .owned(actor)
                .orElseThrow(()
                        -> new NotFoundException(
                        "ORGANIZATION_NOT_FOUND",
                        "No organization application found"
                )
                );
        Instant now = Instant.now();
        Organization replacement = new Organization(
                previous.id(),
                actor,
                input.legalName().trim(),
                input.displayName().trim(),
                input.website(),
                input.industry().trim(),
                input.country().trim(),
                input.registrationNumber(),
                input.contactName().trim(),
                input.contactEmail().trim(),
                input.contactPhone(),
                Organization.Status.DRAFT,
                null,
                previous.createdAt(),
                now
        );
        if (organizations.resubmit(previous.id(), replacement, now) != 1) {
            throw new ConflictException(
                    "REVIEW_STATE_INVALID",
                    "Only a rejected application can be resubmitted"
            );
        }
        // Resubmission opens an editable draft. submit() creates the next immutable snapshot.
        return find(previous.id());
    }

    public List<ApplicationRevisionView> applicationRevisions(
            UUID actor,
            UUID organizationId
    ) {
        organizer(actor);
        if (!organizations.activeMember(organizationId, actor)) {
            throw new AccessDeniedException("Organization membership required");
        }

        return organizations
                .applicationRevisions(organizationId)
                .stream()
                .map(value
                        -> new ApplicationRevisionView(
                        value.getId(),
                        value.getOrganizationId(),
                        value.getRevisionNo(),
                        value.getLegalName(),
                        value.getDisplayName(),
                        value.getWebsite(),
                        value.getIndustry(),
                        value.getCountry(),
                        value.getRegistrationNumber(),
                        value.getContactName(),
                        value.getContactEmail(),
                        value.getContactPhone(),
                        value.getStatus(),
                        value.getReviewerUserId(),
                        value.getReviewReason(),
                        value.getDocumentMediaIds(),
                        value.getSubmittedAt(),
                        value.getReviewedAt()
                )
                )
                .toList();
    }

    public List<ApplicationRevisionView> applicationRevisionsForAdmin(
            UUID organizationId
    ) {
        find(organizationId);
        return organizations
                .applicationRevisions(organizationId)
                .stream()
                .map(value
                        -> new ApplicationRevisionView(
                        value.getId(),
                        value.getOrganizationId(),
                        value.getRevisionNo(),
                        value.getLegalName(),
                        value.getDisplayName(),
                        value.getWebsite(),
                        value.getIndustry(),
                        value.getCountry(),
                        value.getRegistrationNumber(),
                        value.getContactName(),
                        value.getContactEmail(),
                        value.getContactPhone(),
                        value.getStatus(),
                        value.getReviewerUserId(),
                        value.getReviewReason(),
                        value.getDocumentMediaIds(),
                        value.getSubmittedAt(),
                        value.getReviewedAt()
                )
                )
                .toList();
    }

    @Transactional
    public Organization reviewRevision(
            UUID revisionId,
            UUID actor,
            Review input
    ) {
        if (!users.isActiveAdmin(actor)) {
            throw new AccessDeniedException(
                    "Active administrator required"
            );
        }
        if (input.decision() != Organization.Status.APPROVED
                && input.decision() != Organization.Status.REJECTED) {
            throw new BadRequestException(
                    "INVALID_DECISION",
                    "A final decision is required"
            );
        }
        var revision = organizations
                .applicationRevision(revisionId)
                .orElseThrow(()
                        -> new NotFoundException(
                        "APPLICATION_REVISION_NOT_FOUND",
                        "Application revision not found"
                )
                );
        if (input.decision() == Organization.Status.REJECTED
                && (input.reason() == null || input.reason().isBlank())) {
            throw new BadRequestException(
                    "REASON_REQUIRED",
                    "Provide a rejection reason"
            );
        }
        var latest = organizations
                .latestRevision(revision.getOrganizationId())
                .orElseThrow();
        if (!latest.getId().equals(revisionId)) {
            throw new ConflictException(
                    "STALE_APPLICATION_REVISION",
                    "Review the current submission"
            );
        }
        if (organizations.reviewRevision(
                revisionId,
                actor,
                input.decision(),
                input.reason(),
                Instant.now()
        ) != 1) {
            throw new ConflictException(
                    "REVIEW_ALREADY_DECIDED",
                    "This application revision has already been reviewed"
            );
        }
        if (organizations.review(
                revision.getOrganizationId(),
                input.decision(),
                input.reason(),
                Instant.now()
        ) != 1) {
            throw new ConflictException(
                    "REVIEW_STATE_INVALID",
                    "The organization application is no longer pending"
            );
        }
        organizations.logReview(
                revision.getOrganizationId(),
                actor,
                input.decision(),
                input.reason(),
                Instant.now()
        );
        return find(revision.getOrganizationId());
    }

    public Organization mine(UUID actor) {
        organizer(actor);
        return organizations
                .owned(actor)
                .or(() -> organizations.memberOrganization(actor))
                .orElseThrow(()
                        -> new NotFoundException(
                        "ORGANIZATION_NOT_FOUND",
                        "No organization application found"
                )
                );
    }

    public List<Organization> memberships(UUID actor) {
        organizer(actor);
        return organizations.memberships(actor);
    }

    public Organization get(UUID id, UUID actor, boolean admin) {
        Organization o = find(id);
        if (!admin
                && (users
                        .find(actor)
                        .filter(u -> u.active() && "ORGANIZER".equals(u.role()))
                        .isEmpty()
                || !organizations.activeMember(id, actor))) {
            throw new AccessDeniedException("Organization membership required");
        }
        return o;
    }

    public List<OrganizationReviewView> reviews(UUID id) {
        find(id);
        return organizations.reviews(id);
    }

    public List<Organization> pending() {
        return organizations.pending();
    }

    @Transactional
    public Organization review(UUID id, UUID actor, Review input) {
        var latest = organizations
                .latestRevision(id)
                .orElseThrow(()
                        -> new ConflictException(
                        "APPLICATION_SNAPSHOT_REQUIRED",
                        "Submit an application before review"
                )
                );
        return reviewRevision(latest.getId(), actor, input);
    }

    @Transactional
    public Organization update(UUID id, UUID actor, Update input) {
        allow(id, actor, "MANAGE_PROFILE");
        organizations.update(
                id,
                input.displayName().trim(),
                input.website(),
                input.industry().trim(),
                input.contactPhone(),
                Instant.now()
        );
        return find(id);
    }

    public List<OrganizationMemberView> members(UUID id, UUID actor) {
        allow(id, actor, "MANAGE_MEMBERS");
        var rows = organizations.members(id);
        return rows
                .stream()
                .map(row
                        -> new OrganizationMemberView(
                        row.userId(),
                        row.active(),
                        users
                                .find(row.userId())
                                .map(IdentityAccessQuery.Account::email)
                                .orElse(null),
                        organizations.grants(id, row.userId())
                )
                )
                .toList();
    }

    @Transactional
    public void addMember(UUID id, UUID actor, String email) {
        allow(id, actor, "MANAGE_MEMBERS");
        throw new ConflictException(
                "INVITATION_REQUIRED",
                "Invite the organizer and wait for acceptance"
        );
    }

    @Transactional
    public void setGrant(UUID id, UUID actor, UUID member, Grant input) {
        allow(id, actor, "MANAGE_MEMBERS");
        if (!organizations.activeMember(id, member)) {
            throw new NotFoundException(
                    "MEMBER_NOT_FOUND",
                    "Active member not found"
            );
        }
        if (find(id).ownerUserId().equals(member) && !input.active()) {
            throw new BadRequestException(
                    "OWNER_PROTECTED",
                    "Owner authority cannot be revoked"
            );
        }
        organizations.grant(
                id,
                member,
                input.authority().name(),
                actor,
                input.active(),
                Instant.now()
        );
    }

    @Transactional
    public void removeMember(UUID id, UUID actor, UUID member) {
        allow(id, actor, "MANAGE_MEMBERS");
        if (find(id).ownerUserId().equals(member)) {
            throw new BadRequestException(
                    "OWNER_PROTECTED",
                    "Owner cannot be removed"
            );
        }
        if (organizations.deactivate(id, member) == 0) {
            throw new NotFoundException("MEMBER_NOT_FOUND", "Member not found");
        }
    }

    public boolean can(UUID id, UUID actor, String authority) {
        return (users
                .find(actor)
                .filter(u -> "ORGANIZER".equals(u.role()) && u.active())
                .isPresent() && organizations.hasGrant(id, actor, authority));
    }

    public record Create(
            String legalName,
            String displayName,
            String website,
            String industry,
            String country,
            String registrationNumber,
            String contactName,
            String contactEmail,
            String contactPhone
            ) {

    }

    public record Update(
            String displayName,
            String website,
            String industry,
            String contactPhone
            ) {

    }

    public record Review(Organization.Status decision, String reason) {

    }

    public enum Authority {
        MANAGE_PROFILE,
        MANAGE_MEMBERS,
        MANAGE_CONTENT,
        ISSUE_CERTIFICATES,
    }

    public record Grant(Authority authority, boolean active) {

    }
}
