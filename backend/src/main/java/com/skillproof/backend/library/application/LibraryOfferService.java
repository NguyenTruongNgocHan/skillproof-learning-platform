package com.skillproof.backend.library.application;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.skillproof.backend.commerce.contract.ProductOfferQuery;
import com.skillproof.backend.commerce.domain.ProductType;
import com.skillproof.backend.common.exception.NotFoundException;
import com.skillproof.backend.library.infrastructure.persistence.LibraryResourceRepository;

@Service
public class LibraryOfferService implements com.skillproof.backend.library.contract.LibraryOfferQuery {

    private final com.skillproof.backend.organization.contract.OrganizationPublicQuery organizations;
    private final LibraryResourceRepository resources;

    public LibraryOfferService(LibraryResourceRepository resources, com.skillproof.backend.organization.contract.OrganizationPublicQuery organizations) {
        this.resources = resources;
        this.organizations = organizations;
    }

    public ProductOfferQuery.Offer offer(UUID id) {
        var r = resources.findById(id).orElseThrow(() -> new NotFoundException("LIBRARY_RESOURCE_NOT_FOUND", "Resource not found"));
        return new ProductOfferQuery.Offer(ProductType.RESOURCE, id, r.getOrganizationId(), r.getTitle(), r.getPriceVnd(), "PUBLIC".equals(r.getAccessMode()), "PUBLISHED".equals(r.getStatus()) && (r.getOrganizationId() == null || organizations.find(r.getOrganizationId()).map(o -> o.approved()).orElse(false)));
    }
}
