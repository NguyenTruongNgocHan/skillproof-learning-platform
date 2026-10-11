package com.skillproof.backend.organization.application;

import com.skillproof.backend.identity.contract.IdentityAccessQuery;
import com.skillproof.backend.organization.contract.OrganizationAuthorityQuery;
import com.skillproof.backend.organization.infrastructure.OrganizationRepository;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class OrganizationAuthorityQueryService implements OrganizationAuthorityQuery {

    private final OrganizationRepository organizations;
    private final IdentityAccessQuery identities;

    public OrganizationAuthorityQueryService(
            OrganizationRepository organizations, IdentityAccessQuery identities) {
        this.organizations = organizations;
        this.identities = identities;
    }

    @Override
    public boolean activeMember(UUID org, UUID actor) {
        return organizations.activeMember(org, actor);
    }

    @Override
    public boolean hasAuthority(UUID organizationId, UUID actorId, String authority) {
        return identities.isActiveOrganizer(actorId)
                && organizations.hasGrant(organizationId, actorId, authority);
    }
}
