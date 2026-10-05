package com.skillproof.backend.organization.infrastructure;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.skillproof.backend.organization.domain.Organization;
import com.skillproof.backend.organization.domain.OrganizationMemberView;
import com.skillproof.backend.organization.domain.OrganizationReviewView;

@Repository
public class OrganizationRepository {

    private final OrganizationJpaRepository organizations;
    private final OrganizationReviewJpaRepository reviews;
    private final OrganizationMembershipJpaRepository memberships;
    private final OrganizationAuthorityGrantJpaRepository grants;
    private final OrganizationApplicationRevisionJpaRepository revisions;

    public OrganizationRepository(
        OrganizationJpaRepository organizations,
        OrganizationReviewJpaRepository reviews,
        OrganizationMembershipJpaRepository memberships,
        OrganizationAuthorityGrantJpaRepository grants,
        OrganizationApplicationRevisionJpaRepository revisions
    ) {
        this.organizations = organizations;
        this.reviews = reviews;
        this.memberships = memberships;
        this.grants = grants;
        this.revisions = revisions;
    }

    public Optional<Organization> find(UUID id) {
        return organizations.findById(id).map(OrganizationEntity::toDomain);
    }

    public Optional<Organization> publicView(UUID id) {
        return find(id);
    }

    public Optional<Organization> owned(UUID user) {
        return organizations
            .findByOwnerUserId(user)
            .map(OrganizationEntity::toDomain);
    }

    public Optional<Organization> memberOrganization(UUID user) {
        return organizations
            .findMembershipOrganizations(user)
            .stream()
            .findFirst()
            .map(OrganizationEntity::toDomain);
    }

    public List<Organization> memberships(UUID user) {
        return organizations
            .findMembershipOrganizations(user)
            .stream()
            .map(OrganizationEntity::toDomain)
            .toList();
    }

    public List<Organization> pending() {
        return organizations
            .findByStatusOrderByCreatedAtAsc(Organization.Status.PENDING)
            .stream()
            .map(OrganizationEntity::toDomain)
            .toList();
    }

    public void create(Organization organization) {
        organizations.saveAndFlush(new OrganizationEntity(organization));
    }

    public OrganizationApplicationRevisionEntity createRevision(
        Organization organization,
        List<UUID> documentMediaIds,
        Instant submittedAt
    ) {
        int next = revisions
            .findFirstByOrganizationIdOrderByRevisionNoDesc(organization.id())
            .map(value -> value.getRevisionNo() + 1)
            .orElse(1);
        return revisions.saveAndFlush(
            new OrganizationApplicationRevisionEntity(
                UUID.randomUUID(),
                organization.id(),
                next,
                organization.legalName(),
                organization.displayName(),
                organization.website(),
                organization.industry(),
                organization.country(),
                organization.registrationNumber(),
                organization.contactName(),
                organization.contactEmail(),
                organization.contactPhone(),
                documentMediaIds,
                submittedAt
            )
        );
    }

    public Optional<Organization> lockApplication(UUID id) {
        return organizations
            .findForUpdate(id)
            .map(OrganizationEntity::toDomain);
    }

    public Optional<OrganizationApplicationRevisionEntity> latestRevision(
        UUID organizationId
    ) {
        return revisions.findFirstByOrganizationIdOrderByRevisionNoDesc(
            organizationId
        );
    }

    public List<OrganizationApplicationRevisionEntity> applicationRevisions(
        UUID organizationId
    ) {
        return revisions.findByOrganizationIdOrderByRevisionNoDesc(
            organizationId
        );
    }

    public Optional<OrganizationApplicationRevisionEntity> applicationRevision(
        UUID revisionId
    ) {
        return revisions.findById(revisionId);
    }

    public int reviewRevision(
        UUID revisionId,
        UUID reviewer,
        Organization.Status decision,
        String reason,
        Instant reviewedAt
    ) {
        var revision = revisions.findForUpdate(revisionId);
        if (
            revision.isEmpty() || !"PENDING".equals(revision.get().getStatus())
        ) return 0;
        revision.get().review(decision.name(), reviewer, reason, reviewedAt);
        revisions.flush();
        return 1;
    }

    public int resubmit(UUID id, Organization replacement, Instant now) {
        Optional<OrganizationEntity> current = organizations.findForUpdate(id);
        if (
            current.isEmpty() ||
            current.get().toDomain().status() != Organization.Status.REJECTED
        ) {
            return 0;
        }

        current.get().resubmit(replacement, now);
        organizations.flush();
        return 1;
    }

    public int saveDraft(UUID id, Organization replacement, Instant now) {
        Optional<OrganizationEntity> current = organizations.findForUpdate(id);
        if (
            current.isEmpty() ||
            current.get().toDomain().status() != Organization.Status.DRAFT
        ) {
            return 0;
        }
        current.get().saveDraft(replacement, now);
        organizations.flush();
        return 1;
    }

    public int submitDraft(UUID id, Instant now) {
        Optional<OrganizationEntity> current = organizations.findForUpdate(id);
        if (
            current.isEmpty() ||
            current.get().toDomain().status() != Organization.Status.DRAFT
        ) {
            return 0;
        }
        current.get().review(Organization.Status.PENDING, null, now);
        organizations.flush();
        return 1;
    }

    public int review(
        UUID id,
        Organization.Status decision,
        String reason,
        Instant now
    ) {
        Optional<OrganizationEntity> current = organizations.findForUpdate(id);
        if (
            current.isEmpty() ||
            current.get().toDomain().status() != Organization.Status.PENDING
        ) {
            return 0;
        }
        current.get().review(decision, reason, now);
        organizations.flush();
        return 1;
    }

    public List<OrganizationReviewView> reviews(UUID organizationId) {
        return reviews
            .findByOrganizationIdOrderByReviewedAtDesc(organizationId)
            .stream()
            .map(review ->
                new OrganizationReviewView(
                    review.getReviewerUserId(),
                    review.getDecision(),
                    review.getReason(),
                    review.getReviewedAt()
                )
            )
            .toList();
    }

    public void logReview(
        UUID organizationId,
        UUID actor,
        Organization.Status decision,
        String reason,
        Instant now
    ) {
        reviews.save(
            new OrganizationReviewEntity(
                organizationId,
                actor,
                decision,
                reason,
                now
            )
        );
    }

    public void update(
        UUID id,
        String display,
        String website,
        String industry,
        String phone,
        Instant now
    ) {
        organizations
            .findById(id)
            .ifPresent(organization ->
                organization.updateProfile(
                    display,
                    website,
                    industry,
                    phone,
                    now
                )
            );
    }

    public void addMember(
        UUID organizationId,
        UUID userId,
        UUID actor,
        Instant now,
        boolean owner
    ) {
        OrganizationMembershipEntity membership = memberships
            .findByOrganizationIdAndUserId(organizationId, userId)
            .map(existing -> {
                if (!existing.isActive()) {
                    grants
                        .findByMembershipIdAndActiveTrue(existing.id())
                        .forEach(grant -> grant.update(false, actor, now));
                }
                existing.activate();
                return existing;
            })
            .orElseGet(() ->
                memberships.saveAndFlush(
                    new OrganizationMembershipEntity(
                        organizationId,
                        userId,
                        now
                    )
                )
            );
        if (owner) {
            for (String authority : List.of(
                "MANAGE_PROFILE",
                "MANAGE_MEMBERS",
                "MANAGE_CONTENT",
                "ISSUE_CERTIFICATES"
            )) {
                OrganizationAuthorityGrantEntity existing = grants
                    .findByMembershipIdAndAuthority(membership.id(), authority)
                    .orElseGet(() ->
                        new OrganizationAuthorityGrantEntity(
                            membership.id(),
                            authority,
                            true,
                            actor,
                            now
                        )
                    );
                existing.update(true, actor, now);
                grants.save(existing);
            }
        }
    }

    public List<OrganizationMemberView> members(UUID organizationId) {
        return memberships
            .findByOrganizationId(organizationId)
            .stream()
            .map(membership ->
                new OrganizationMemberView(
                    membership.getUserId(),
                    membership.isActive(),
                    null,
                    List.of()
                )
            )
            .toList();
    }

    public List<String> grants(UUID organizationId, UUID memberId) {
        return memberships
            .findByOrganizationIdAndUserId(organizationId, memberId)
            .filter(OrganizationMembershipEntity::isActive)
            .stream()
            .flatMap(membership ->
                grants.findByMembershipIdAndActiveTrue(membership.id()).stream()
            )
            .map(OrganizationAuthorityGrantEntity::getAuthority)
            .toList();
    }

    public boolean hasGrant(UUID organizationId, UUID actor, String authority) {
        return organizations
            .findById(organizationId)
            .filter(
                organization ->
                    organization.toDomain().status() ==
                    Organization.Status.APPROVED
            )
            .flatMap(organization ->
                memberships.findByOrganizationIdAndUserId(organizationId, actor)
            )
            .filter(OrganizationMembershipEntity::isActive)
            .map(membership ->
                grants.existsByMembershipIdAndAuthorityAndActiveTrue(
                    membership.id(),
                    authority
                )
            )
            .orElse(false);
    }

    public void grant(
        UUID organizationId,
        UUID memberId,
        String authority,
        UUID actor,
        boolean active,
        Instant now
    ) {
        memberships
            .findByOrganizationIdAndUserId(organizationId, memberId)
            .ifPresent(membership -> {
                OrganizationAuthorityGrantEntity grant = grants
                    .findByMembershipIdAndAuthority(membership.id(), authority)
                    .orElseGet(() ->
                        new OrganizationAuthorityGrantEntity(
                            membership.id(),
                            authority,
                            active,
                            actor,
                            now
                        )
                    );
                grant.update(active, actor, now);
                grants.save(grant);
            });
    }

    public int deactivate(UUID organizationId, UUID memberId) {
        Optional<OrganizationMembershipEntity> membership =
            memberships.findByOrganizationIdAndUserId(organizationId, memberId);
        if (membership.isEmpty()) {
            return 0;
        }
        membership.get().deactivate();
        return 1;
    }

    public boolean activeMember(UUID organizationId, UUID memberId) {
        return memberships.existsByOrganizationIdAndUserIdAndActiveTrue(
            organizationId,
            memberId
        );
    }
}
