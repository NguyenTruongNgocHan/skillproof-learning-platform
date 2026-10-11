package com.skillproof.backend.organization.contract;

import java.util.UUID;

public interface OrganizationAuthorityQuery {

    default boolean activeMember(UUID organizationId, UUID actorId) {
        return false;
    }

    boolean hasAuthority(UUID organizationId, UUID actorId, String authority);
}
