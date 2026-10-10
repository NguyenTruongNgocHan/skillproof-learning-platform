package com.skillproof.backend.library.application;

import java.time.Instant;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.data.domain.PageRequest;
import com.skillproof.backend.library.infrastructure.persistence.*;
import com.skillproof.backend.identity.contract.IdentityAccessQuery;
import com.skillproof.backend.access.contract.AccessEntitlementQuery;
import com.skillproof.backend.commerce.domain.ProductType;
import com.skillproof.backend.common.exception.*;

@Service
public class LibraryResourceService {

    private final com.skillproof.backend.media.contract.LibraryMediaQuery media;
    private final LibraryAuthorAccess authors;
    private final LibraryOfferService offers;
    private final com.skillproof.backend.organization.contract.OrganizationPublicQuery organizations;
    private final LibraryResourceRepository resources;
    private final IdentityAccessQuery identities;
    private final AccessEntitlementQuery entitlements;

    public LibraryResourceService(LibraryResourceRepository resources, IdentityAccessQuery identities, AccessEntitlementQuery entitlements, com.skillproof.backend.media.contract.LibraryMediaQuery media, LibraryAuthorAccess authors, LibraryOfferService offers, com.skillproof.backend.organization.contract.OrganizationPublicQuery organizations) {
        this.authors = authors;
        this.offers = offers;
        this.organizations = organizations;
        this.media = media;
        this.resources = resources;
        this.identities = identities;
        this.entitlements = entitlements;
    }

    public record Summary(UUID id, UUID authorId, String title, String summary, long priceVnd) {

    }

    private Summary summary(LibraryResourceEntity r) {
        return new Summary(r.getId(), r.getAuthorId(), r.getTitle(), r.getSummary(), r.getPriceVnd());
    }

    private LibraryResourceEntity published(UUID id) {
        return resources.findById(id).filter(r -> "PUBLISHED".equals(r.getStatus())).orElseThrow(() -> new NotFoundException("RESOURCE_NOT_FOUND", "Published resource not found"));
    }

    private void author(UUID actor, LibraryResourceEntity r) {
        authors.manage(actor, r);
    }

    private void validate(String title, String summary, String body, long price) {
        if (title == null || title.isBlank() || title.length() > 180 || summary == null || summary.isBlank() || summary.length() > 1000
                || body == null || body.length() > 100000 || !com.skillproof.backend.library.domain.LibraryRules.validPrice(price)) {
            throw new BadRequestException("RESOURCE_INVALID", "Invalid resource content or VND price");
        }
    }

    @Transactional(readOnly = true)
    public List<Summary> discover(int page, int size) {
        return resources.findByStatusOrderByCreatedAtDesc("PUBLISHED", PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100))).stream().filter(r -> {
            var offer = offers.offer(r.getId());
            return offer.available() && offer.purchasable();
        }).map(this::summary).toList();
    }

    @Transactional(readOnly = true)
    public Summary detail(UUID id) {
        var offer = offers.offer(id);
        if (!offer.available() || !offer.purchasable()) {
            throw new NotFoundException("RESOURCE_NOT_FOUND", "Public resource not found");
        }
        return summary(published(id));
    }

    @Transactional(readOnly = true)
    public LibraryResourceEntity content(UUID learner, UUID id) {
        entitlements.require(learner, ProductType.RESOURCE, id);
        return published(id);
    }

    @Transactional(readOnly = true)
    public List<LibraryResourceEntity> mine(UUID author) {
        if (!identities.isActiveLearner(author)) {
            throw new AccessDeniedException("Active learner required");
        
        }return resources.findByAuthorIdOrderByCreatedAtDesc(author);
    }

    @Transactional
    public LibraryResourceEntity create(UUID actor, String title, String summary, String body, long price) {
        if (!identities.isActiveLearner(actor)) {
            throw new AccessDeniedException("Active learner required");
        
        }validate(title, summary, body, price);
        return resources.save(new LibraryResourceEntity(UUID.randomUUID(), actor, null, title.trim(), summary.trim(), body, "DRAFT", price, Instant.now(), null, null));
    }

    @Transactional
    public LibraryResourceEntity edit(UUID actor, UUID id, String title, String summary, String body, long price) {
        var r = resources.lock(id).orElseThrow(() -> new NotFoundException("RESOURCE_NOT_FOUND", "Resource not found"));
        author(actor, r);
        if (!List.of("DRAFT", "REJECTED").contains(r.getStatus())) {
            throw new ConflictException("RESOURCE_IMMUTABLE", "Published or submitted resources cannot be edited");
        }
        if ("RESTRICTED".equals(r.getAccessMode()) && price != 0) {
            throw new BadRequestException("RESOURCE_OFFER", "Restricted resource is not publicly sold");
        }
        validate(title, summary, body, price);
        r.reopenDraft();
        r.edit(title.trim(), summary.trim(), body, price);
        return resources.save(r);
    }

    @Transactional
    public LibraryResourceEntity submit(UUID actor, UUID id) {
        var r = resources.lock(id).orElseThrow(() -> new NotFoundException("RESOURCE_NOT_FOUND", "Resource not found"));
        author(actor, r);
        if (!"DRAFT".equals(r.getStatus())) {
            throw new ConflictException("RESOURCE_STATE", "Draft required");
        }
        if (!com.skillproof.backend.library.domain.LibraryRules.hasContent(r.getBody(), media.hasLibraryFiles(id))) {
            throw new BadRequestException("RESOURCE_EMPTY", "Text or an uploaded file is required");
        }
        r.submitReview();
        return resources.save(r);
    }

    @Transactional
    public LibraryResourceEntity review(UUID admin, UUID id, boolean approved, String reason) {
        if (!identities.isActiveAdmin(admin)) {
            throw new AccessDeniedException("Administrator required");
        }
        var r = resources.lock(id).orElseThrow(() -> new NotFoundException("RESOURCE_NOT_FOUND", "Resource not found"));
        if (!"SUBMITTED".equals(r.getStatus())) {
            throw new ConflictException("RESOURCE_STATE", "Submitted resource required");
        }
        if (reason == null || reason.isBlank() || reason.length() > 1000) {
            throw new BadRequestException("REVIEW_REASON", "Review reason required");
        }
        r.review(approved, admin, reason);
        return resources.save(r);
    }

    @Transactional(readOnly = true)
    public List<LibraryResourceEntity> reviewQueue(UUID admin, int page, int size) {
        if (!identities.isActiveAdmin(admin)) {
            throw new AccessDeniedException("Administrator required");
        }
        return resources.findByStatusOrderByCreatedAtDesc("SUBMITTED", PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100))).stream().toList();
    }

    @Transactional(readOnly = true)
    public LibraryResourceEntity reviewContent(UUID admin, UUID id) {
        if (!identities.isActiveAdmin(admin)) {
            throw new AccessDeniedException("Administrator required");
        }
        return resources.findById(id).orElseThrow(() -> new NotFoundException("RESOURCE_NOT_FOUND", "Resource not found"));
    }

    @Transactional
    public LibraryResourceEntity createOrganization(UUID actor, UUID organization, String title, String summary, String body, long price, String mode) {
        organizations.requireApprovedForUpdate(organization);
        authors.organization(actor, organization);
        validate(title, summary, body, price);
        if (!java.util.List.of("PUBLIC", "RESTRICTED").contains(mode) || "RESTRICTED".equals(mode) && price != 0) {
            throw new BadRequestException("RESOURCE_OFFER", "Use PUBLIC prices or RESTRICTED with zero price");
        }
        var resource = new LibraryResourceEntity(UUID.randomUUID(), actor, organization, title.trim(), summary.trim(), body, "DRAFT", price, Instant.now(), null, null);
        resource.configureAccess(mode);
        return resources.save(resource);
    }

    @Transactional(readOnly = true)
    public List<LibraryResourceEntity> organizationResources(UUID actor, UUID organization) {
        authors.organization(actor, organization);
        return resources.findByOrganizationIdOrderByCreatedAtDesc(organization);
    }

}
