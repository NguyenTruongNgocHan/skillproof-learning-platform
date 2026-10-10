package com.skillproof.backend.library.application;

import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import com.skillproof.backend.identity.contract.IdentityAccessQuery;
import com.skillproof.backend.library.infrastructure.persistence.LibraryResourceEntity;
import com.skillproof.backend.organization.contract.OrganizationAuthorityQuery;

@Component
public class LibraryAuthorAccess {

    private final IdentityAccessQuery identities;
    private final OrganizationAuthorityQuery organizations;

    public LibraryAuthorAccess(IdentityAccessQuery identities, OrganizationAuthorityQuery organizations) {
        this.identities = identities;
        this.organizations = organizations;
    }

    public void organization(UUID actor, UUID organization) {
        if (!organizations.hasAuthority(organization, actor, "MANAGE_CONTENT")) {
            throw new AccessDeniedException("Approved organization content authority required");
        }
    }

    public void manage(UUID actor, LibraryResourceEntity resource) {
        if (resource.getOrganizationId() != null) {
            organization(actor, resource.getOrganizationId()); 
        }else if (!identities.isActiveLearner(actor) || !actor.equals(resource.getAuthorId())) {
            throw new AccessDeniedException("Resource author required");
        }
    }

    public boolean canRead(UUID actor, LibraryResourceEntity resource) {
        if (identities.isActiveAdmin(actor)) {
            return true;
        }
        try {
            manage(actor, resource);
            return true;
        } catch (AccessDeniedException denied) {
            return false;
        }
    }
}
