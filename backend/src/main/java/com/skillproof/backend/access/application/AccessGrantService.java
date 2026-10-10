package com.skillproof.backend.access.application;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;
import com.skillproof.backend.access.contract.PaymentEntitlementWriter;
import com.skillproof.backend.access.infrastructure.persistence.*;
import com.skillproof.backend.commerce.contract.ProductOfferQuery;
import com.skillproof.backend.commerce.domain.ProductType;
import com.skillproof.backend.common.exception.*;
import com.skillproof.backend.identity.contract.IdentityAccessQuery;
import com.skillproof.backend.organization.contract.*;

@Service
public class AccessGrantService implements PaymentEntitlementWriter {

    private final AccessGrantRepository grants;
    private final ProductOfferQuery offers;
    private final OrganizationAuthorityQuery authorities;
    private final OrganizationPublicQuery organizations;
    private final IdentityAccessQuery identities;

    public AccessGrantService(AccessGrantRepository grants, ProductOfferQuery offers, OrganizationAuthorityQuery authorities,
            OrganizationPublicQuery organizations, IdentityAccessQuery identities) {
        this.grants = grants;
        this.offers = offers;
        this.authorities = authorities;
        this.organizations = organizations;
        this.identities = identities;
    }

    @Transactional
    public AccessGrantEntity grantOrganization(UUID actor, UUID organization, UUID learner, ProductType type,
            UUID product, Instant expiresAt) {
        organizations.requireApprovedForUpdate(organization);
        if (!authorities.hasAuthority(organization, actor, "MANAGE_CONTENT")) {
            throw new AccessDeniedException("Content authority required");
        }
        var offer = offers.offer(type, product);
        if (!organization.equals(offer.organizationId()) || !offer.available()) {
            throw new BadRequestException("GRANT_PRODUCT", "Product must belong to this approved organization");
        }
        if (!identities.isActiveLearner(learner)) {
            throw new BadRequestException("GRANT_LEARNER", "Active learner required");
        }
        if (expiresAt != null && !expiresAt.isAfter(Instant.now())) {
            throw new BadRequestException("GRANT_EXPIRY", "Expiry must be in the future");
        }
        return grants.save(new AccessGrantEntity(UUID.randomUUID(), learner, type.name(), product, "ORGANIZATION",
                UUID.randomUUID(), actor, Instant.now(), expiresAt, null));
    }

    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.MANDATORY)
    @Override
    public void grantPayment(UUID learner, ProductType type, UUID product, UUID order) {
        // Payment has already locked its order. The grant is atomic with the paid status.
        if (!grants.existsByReferenceId(order)) {
            grants.save(new AccessGrantEntity(UUID.randomUUID(), learner,
                    type.name(), product, "PAYMENT", order, null, Instant.now(), null, null));
        }
    }

    @Transactional
    public void revoke(UUID actor, UUID organization, UUID id) {
        organizations.requireApprovedForUpdate(organization);
        if (!authorities.hasAuthority(organization, actor, "MANAGE_CONTENT")) {
            throw new AccessDeniedException("Content authority required");
        }
        var g = grants.lock(id).orElseThrow(() -> new NotFoundException("GRANT_NOT_FOUND", "Grant not found"));
        var offer = offers.offer(ProductType.valueOf(g.getProductType()), g.getProductId());
        if (!"ORGANIZATION".equals(g.getSource()) || !organization.equals(offer.organizationId())) {
            throw new AccessDeniedException("Organization grant required");
        }
        g.revoke(Instant.now());
    }

    @Transactional(readOnly = true)
    public List<AccessGrantEntity> mine(UUID learner) {
        if (!identities.isActiveLearner(learner)) {
            throw new AccessDeniedException("Active learner required");
        }
        return grants.findByLearnerIdOrderByCreatedAtDesc(learner);
    }
}
