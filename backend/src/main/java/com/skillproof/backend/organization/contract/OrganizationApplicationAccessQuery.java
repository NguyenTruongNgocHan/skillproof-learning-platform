package com.skillproof.backend.organization.contract;

import java.util.UUID;

/**
 * Private application-media authorization projection. Not a catalog contract.
 */
public interface OrganizationApplicationAccessQuery {

    boolean isOwner(UUID organizationId, UUID actorId);

    boolean lockEditableApplication(UUID organizationId, UUID actorId);

    boolean documentInSubmission(UUID organizationId, UUID mediaId);
}
