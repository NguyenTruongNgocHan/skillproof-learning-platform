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

    public OrganizationRepository(OrganizationJpaRepository organizations,
            OrganizationReviewJpaRepository reviews,
            OrganizationMembershipJpaRepository memberships,
            OrganizationAuthorityGrantJpaRepository grants) {
        this.organizations = organizations;
        this.reviews = reviews;
        this.memberships = memberships;
        this.grants = grants;
    }

    public Optional<Organization> find(UUID id) {
        return organizations.findById(id).map(OrganizationEntity::toDomain);
    }

    public Optional<Organization> publicView(UUID id) {
        return find(id);
    }

    public Optional<Organization> owned(UUID user) {
        return organizations.findByOwnerUserId(user).map(OrganizationEntity::toDomain);
    }

    public Optional<Organization> memberOrganization(UUID user) {
        return organizations.findMembershipOrganizations(user).stream()
                .findFirst().map(OrganizationEntity::toDomain);
    }

    public List<Organization> memberships(UUID user) {
        return organizations.findMembershipOrganizations(user).stream()
                .map(OrganizationEntity::toDomain).toList();
    }

    public List<Organization> pending() {
        return organizations.findByStatusOrderByCreatedAtAsc(Organization.Status.PENDING)
                .stream().map(OrganizationEntity::toDomain).toList();
    }

    public void create(Organization organization) {
        organizations.saveAndFlush(new OrganizationEntity(organization));
    }

    public int resubmit(UUID id, Organization replacement, Instant now) {
        Optional<OrganizationEntity> current = organizations.findForUpdate(id);
        if (current.isEmpty() || current.get().toDomain().status() != Organization.Status.REJECTED) {
            return 0;
        }
        current.get().resubmit(replacement, now);
        organizations.flush();
        return 1;
    }

    public int review(UUID id, Organization.Status decision, String reason, Instant now) {
        Optional<OrganizationEntity> current = organizations.findForUpdate(id);
        if (current.isEmpty() || current.get().toDomain().status() != Organization.Status.PENDING) {
            return 0;
        }
        current.get().review(decision, reason, now);
        organizations.flush();
        return 1;
    }

    public List<OrganizationReviewView> reviews(UUID organizationId) {
        return reviews.findByOrganizationIdOrderByReviewedAtDesc(organizationId).stream()
                .map(review -> new OrganizationReviewView(review.getReviewerUserId(),
                review.getDecision(), review.getReason(), review.getReviewedAt())).toList();
    }

    public void logReview(UUID organizationId, UUID actor,
            Organization.Status decision, String reason, Instant now) {
        reviews.save(new OrganizationReviewEntity(organizationId, actor, decision, reason, now));
    }

    public void update(UUID id, String display, String website,
            String industry, String phone, Instant now) {
        organizations.findById(id).ifPresent(organization
                -> organization.updateProfile(display, website, industry, phone, now));
    }

    public void addMember(UUID organizationId, UUID userId, UUID actor,
            Instant now, boolean owner) {
        OrganizationMembershipEntity membership = memberships.findByOrganizationIdAndUserId(organizationId, userId)
                .map(existing -> {
                    existing.activate();
                    return existing;
                })
                .orElseGet(() -> memberships.saveAndFlush(
                        new OrganizationMembershipEntity(organizationId, userId, now)));
        if (owner) {
            for (String authority : List.of("MANAGE_PROFILE", "MANAGE_MEMBERS",
                    "MANAGE_CONTENT", "ISSUE_CERTIFICATES")) {
                grants.save(new OrganizationAuthorityGrantEntity(
                        membership.id(), authority, true, actor, now));
            }
        }
    }

    public List<OrganizationMemberView> members(UUID organizationId) {
        return memberships.findByOrganizationId(organizationId).stream()
                .map(membership -> new OrganizationMemberView(membership.getUserId(),
                membership.isActive(), null, List.of())).toList();
    }

    public List<String> grants(UUID organizationId, UUID memberId) {
        return memberships.findByOrganizationIdAndUserId(organizationId, memberId)
                .filter(OrganizationMembershipEntity::isActive)
                .stream().flatMap(membership -> grants.findByMembershipIdAndActiveTrue(
                membership.id()).stream())
                .map(OrganizationAuthorityGrantEntity::getAuthority).toList();
    }

    public boolean hasGrant(UUID organizationId, UUID actor, String authority) {
        return organizations.findById(organizationId)
                .filter(organization -> organization.toDomain().status() == Organization.Status.APPROVED)
                .flatMap(organization -> memberships.findByOrganizationIdAndUserId(organizationId, actor))
                .filter(OrganizationMembershipEntity::isActive)
                .map(membership -> grants.existsByMembershipIdAndAuthorityAndActiveTrue(
                membership.id(), authority)).orElse(false);
    }

    public void grant(UUID organizationId, UUID memberId, String authority,
            UUID actor, boolean active, Instant now) {
        memberships.findByOrganizationIdAndUserId(organizationId, memberId)
                .ifPresent(membership -> {
                    OrganizationAuthorityGrantEntity grant = grants
                            .findByMembershipIdAndAuthority(membership.id(), authority)
                            .orElseGet(() -> new OrganizationAuthorityGrantEntity(
                            membership.id(), authority, active, actor, now));
                    grant.update(active, actor, now);
                    grants.save(grant);
                });
    }

    public int deactivate(UUID organizationId, UUID memberId) {
        Optional<OrganizationMembershipEntity> membership = memberships
                .findByOrganizationIdAndUserId(organizationId, memberId);
        if (membership.isEmpty()) {
            return 0;
        }
        membership.get().deactivate();
        return 1;
    }

    public boolean activeMember(UUID organizationId, UUID memberId) {
        return memberships.existsByOrganizationIdAndUserIdAndActiveTrue(organizationId, memberId);
    }
}
