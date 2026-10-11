package com.skillproof.backend.organization.contract;

import java.time.Instant;
import java.util.UUID;

public interface IssuerAuthorizationQuery {
    record Authorization(
            UUID id, UUID organizationId, String scope, String evidenceIds, Instant validUntil) {}

    void requireAuthorized(UUID organization);

    Authorization authorization(UUID organization, UUID courseVersion);
}
