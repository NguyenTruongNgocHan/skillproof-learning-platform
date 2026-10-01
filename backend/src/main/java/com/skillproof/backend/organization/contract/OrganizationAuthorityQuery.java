package com.skillproof.backend.organization.contract;

import java.util.UUID;

public interface OrganizationAuthorityQuery {

    boolean hasAuthority(UUID organizationId, UUID actorId, String authority);
}
