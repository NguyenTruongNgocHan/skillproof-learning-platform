package com.skillproof.backend.library.application;

import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillproof.backend.access.contract.AccessEntitlementQuery;
import com.skillproof.backend.commerce.domain.ProductType;
import com.skillproof.backend.common.exception.NotFoundException;
import com.skillproof.backend.identity.contract.IdentityAccessQuery;
import com.skillproof.backend.library.contract.LibraryMediaAccess;
import com.skillproof.backend.library.infrastructure.persistence.LibraryResourceRepository;

@Service
public class LibraryMediaAccessService implements LibraryMediaAccess {

    private final LibraryAuthorAccess authors;
    private final LibraryResourceRepository resources;
    private final IdentityAccessQuery identities;
    private final AccessEntitlementQuery entitlements;

    public LibraryMediaAccessService(LibraryResourceRepository resources, IdentityAccessQuery identities, AccessEntitlementQuery entitlements, LibraryAuthorAccess authors) {
        this.authors = authors;
        this.resources = resources;
        this.identities = identities;
        this.entitlements = entitlements;
    }

    @Override
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.MANDATORY)
    public void requireWrite(UUID actor, UUID id) {
        var r = resources.lock(id).orElseThrow(() -> new NotFoundException("RESOURCE_NOT_FOUND", "Resource not found"));
        authors.manage(actor, r);
        if (!"DRAFT".equals(r.getStatus())) {
            throw new AccessDeniedException("Only the author's draft resource is editable");
        }
    }

    @Override
    public void requireRead(UUID actor, UUID id) {
        var r = resources.findById(id).orElseThrow(() -> new NotFoundException("RESOURCE_NOT_FOUND", "Resource not found"));
        if (authors.canRead(actor, r)) {
            return;
        }
        entitlements.require(actor, ProductType.RESOURCE, id);
    }
}
