package com.skillproof.backend.organization.contract;

import java.util.Optional;
import java.util.UUID;

/**
 * Public organization projection for catalog presentation and publication
 * checks.
 */
public interface OrganizationPublicQuery {

    record OrganizationView(UUID id, String displayName, String status) {

        public boolean approved() {
            return "APPROVED".equals(status);
        }
    }

    Optional<OrganizationView> find(UUID organizationId);
}
