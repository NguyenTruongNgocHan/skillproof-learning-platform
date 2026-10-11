package com.skillproof.backend.access.application;

import com.skillproof.backend.access.contract.PaymentEntitlementWriter;
import com.skillproof.backend.access.infrastructure.persistence.*;
import com.skillproof.backend.commerce.contract.ProductOfferQuery;
import com.skillproof.backend.commerce.domain.ProductType;
import com.skillproof.backend.common.exception.*;
import com.skillproof.backend.identity.contract.IdentityAccessQuery;
import com.skillproof.backend.organization.contract.*;

import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class AccessGrantService implements PaymentEntitlementWriter {
    @org.springframework.beans.factory.annotation.Autowired
    private com.skillproof.backend.operations.contract.ActivityJournal journal;

    private final AccessGrantRepository grants;
    private final ProductOfferQuery offers;
    private final OrganizationAuthorityQuery authorities;
    private final OrganizationPublicQuery organizations;
    private final IdentityAccessQuery identities;

    public AccessGrantService(
            AccessGrantRepository grants,
            ProductOfferQuery offers,
            OrganizationAuthorityQuery authorities,
            OrganizationPublicQuery organizations,
            IdentityAccessQuery identities) {
        this.grants = grants;
        this.offers = offers;
        this.authorities = authorities;
        this.organizations = organizations;
        this.identities = identities;
    }

    @Transactional
    public AccessGrantEntity grantOrganization(
            UUID actor,
            UUID organization,
            UUID learner,
            ProductType type,
            UUID product,
            Instant expiresAt) {
        organizations.requireApprovedForUpdate(organization);
        if (!authorities.hasAuthority(organization, actor, "MANAGE_CONTENT")) {
            throw new AccessDeniedException("Content authority required");
        }
        var offer = offers.offer(type, product);
        if (!organization.equals(offer.organizationId()) || !offer.available()) {
            throw new BadRequestException(
                    "GRANT_PRODUCT", "Product must belong to this approved organization");
        }
        if (!identities.isActiveLearner(learner)) {
            throw new BadRequestException("GRANT_LEARNER", "Active learner required");
        }
        if (expiresAt != null && !expiresAt.isAfter(Instant.now())) {
            throw new BadRequestException("GRANT_EXPIRY", "Expiry must be in the future");
        }
        var existing =
                grants
                        .findByLearnerIdAndProductTypeAndProductId(learner, type.name(), product)
                        .stream()
                        .filter(
                                g ->
                                        organization.equals(g.getOrganizationId())
                                                && g.getRevokedAt() == null
                                                && (g.getExpiresAt() == null
                                                        || g.getExpiresAt().isAfter(Instant.now())))
                        .findFirst();
        if (existing.isPresent()) return existing.get();
        var grant =
                new AccessGrantEntity(
                        UUID.randomUUID(),
                        learner,
                        type.name(),
                        product,
                        "ORGANIZATION",
                        UUID.randomUUID(),
                        actor,
                        Instant.now(),
                        expiresAt,
                        null);
        grant.setOrganizationId(organization);
        grants.save(grant);
        journal.record(actor, organization, "ACCESS_GRANTED", grant.getId(), type.name());
        journal.notify(
                learner,
                organization,
                "ACCESS",
                "Organization granted " + type.name() + " access",
                "/app/learning");
        return grant;
    }

    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.MANDATORY)
    @Override
    public void grantPayment(UUID learner, ProductType type, UUID product, UUID order) {
        // Payment has already locked its order. The grant is atomic with the paid status.
        if (!grants.existsByReferenceId(order)) {
            grants.save(
                    new AccessGrantEntity(
                            UUID.randomUUID(),
                            learner,
                            type.name(),
                            product,
                            "PAYMENT",
                            order,
                            null,
                            Instant.now(),
                            null,
                            null));
        }
    }

    @Transactional
    public void revoke(UUID actor, UUID organization, UUID id) {
        organizations.requireApprovedForUpdate(organization);
        if (!authorities.hasAuthority(organization, actor, "MANAGE_CONTENT")) {
            throw new AccessDeniedException("Content authority required");
        }
        var g =
                grants.lock(id)
                        .orElseThrow(
                                () -> new NotFoundException("GRANT_NOT_FOUND", "Grant not found"));
        var offer = offers.offer(ProductType.valueOf(g.getProductType()), g.getProductId());
        if (!"ORGANIZATION".equals(g.getSource()) || !organization.equals(offer.organizationId())) {
            throw new AccessDeniedException("Organization grant required");
        }
        g.revoke(Instant.now());
        journal.record(actor, organization, "ACCESS_REVOKED", id, g.getProductType());
    }

    @Transactional(readOnly = true)
    public List<AccessGrantEntity> mine(UUID learner) {
        if (!identities.isActiveLearner(learner)) {
            throw new AccessDeniedException("Active learner required");
        }
        return grants.findByLearnerIdOrderByCreatedAtDesc(learner);
    }

    public org.springframework.data.domain.Page<AccessGrantEntity> organization(
            UUID actor, UUID org, int page) {
        if (!authorities.hasAuthority(org, actor, "MANAGE_CONTENT"))
            throw new AccessDeniedException("Content authority required");
        return grants.findByOrganizationIdOrderByCreatedAtDesc(
                org, PageRequest.of(Math.max(0, page), 25));
    }

    public void requireImport(
            UUID actor, UUID organization, ProductType type, UUID product, Instant expiry) {
        if (!authorities.hasAuthority(organization, actor, "MANAGE_CONTENT"))
            throw new AccessDeniedException("Content authority required");
        var offer = offers.offer(type, product);
        if (!organization.equals(offer.organizationId()) || !offer.available())
            throw new BadRequestException(
                    "GRANT_PRODUCT", "Select an available product owned by this organization");
        if (expiry != null && !expiry.isAfter(Instant.now()))
            throw new BadRequestException("GRANT_EXPIRY", "Expiry must be in the future");
    }

    @Transactional
    public AccessGrantEntity grantImported(
            UUID actor,
            UUID organization,
            UUID learner,
            ProductType type,
            UUID product,
            Instant expiry,
            String cohort) {
        var grant = grantOrganization(actor, organization, learner, type, product, expiry);
        if (grant.getCohort().isBlank() && !cohort.isBlank()) {
            grant.setCohort(cohort);
            journal.record(actor, organization, "ACCESS_COHORT", grant.getId(), cohort);
        }
        return grant;
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<AccessGrantEntity> search(
            UUID actor, UUID organization, String query, String status, int page) {
        if (!authorities.hasAuthority(organization, actor, "MANAGE_CONTENT"))
            throw new AccessDeniedException("Content authority required");
        if (!java.util.Set.of("", "ACTIVE", "EXPIRED", "REVOKED").contains(status))
            throw new BadRequestException("GRANT_FILTER", "Invalid grant status");
        return grants.search(
                organization,
                query.trim(),
                status,
                Instant.now(),
                PageRequest.of(Math.max(0, page), 100));
    }
}
