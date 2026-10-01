package com.skillproof.backend.organization.infrastructure;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.skillproof.backend.common.exception.BadRequestException;
import com.skillproof.backend.identity.application.OrganizationRoleTransitionPolicy;
import com.skillproof.backend.identity.domain.UserRole;

/**
 * Keeps organization membership consistent when an administrator changes an account role.
 */
@Component
public class OrganizationRoleTransitionPolicyAdapter implements OrganizationRoleTransitionPolicy {

    private final OrganizationRepository organizations;

    public OrganizationRoleTransitionPolicyAdapter(OrganizationRepository organizations) {
        this.organizations = organizations;
    }

    @Override
    public void assertTransitionAllowed(UUID accountId, UserRole target) {
        if (target != UserRole.ORGANIZER && organizations.memberOrganization(accountId).isPresent()) {
            throw new BadRequestException(
                    "IDENTITY_ROLE_CHANGE_ORGANIZATION_MEMBERSHIP",
                    "Remove the account from its organization before changing it from ORGANIZER."
            );
        }
    }
}
