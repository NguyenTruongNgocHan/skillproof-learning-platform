package com.skillproof.backend.learning.application;

import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import com.skillproof.backend.identity.contract.IdentityAccessQuery;
import com.skillproof.backend.organization.contract.OrganizationAuthorityQuery;

@Component
public class LearningAccess {

    private final IdentityAccessQuery identities;
    private final OrganizationAuthorityQuery organizations;

    public LearningAccess(IdentityAccessQuery identities, OrganizationAuthorityQuery organizations) {
        this.identities = identities;
        this.organizations = organizations;
    }

    public void learner(UUID user) {
        if (!identities.isActiveLearner(user)) {
            throw new AccessDeniedException("Active learner required");
    
        }}

    public void organizer(UUID user, UUID organization) {
        if (!identities.isActiveOrganizer(user) || !organizations.hasAuthority(organization, user, "MANAGE_CONTENT")) {
            throw new AccessDeniedException("Approved organization content authority required");
    
        }}
}
