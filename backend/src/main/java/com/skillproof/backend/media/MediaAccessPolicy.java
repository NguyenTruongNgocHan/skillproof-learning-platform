package com.skillproof.backend.media;

import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import com.skillproof.backend.identity.contract.IdentityAccessQuery;
import com.skillproof.backend.learning.contract.LearningResourceAccessQuery;
import com.skillproof.backend.organization.contract.OrganizationAuthorityQuery;

/**
 * Media owns storage authorization orchestration; Learning remains authority
 * for learning-resource access.
 */
@Component
public class MediaAccessPolicy {

    private final IdentityAccessQuery identities;
    private final LearningResourceAccessQuery learning;
    private final OrganizationAuthorityQuery organizations;

    public MediaAccessPolicy(IdentityAccessQuery identities, LearningResourceAccessQuery learning, OrganizationAuthorityQuery organizations) {
        this.identities = identities;
        this.learning = learning;
        this.organizations = organizations;
    }

    void active(UUID actor) {
        if (identities.find(actor).filter(IdentityAccessQuery.Account::active).isEmpty()) {
            throw new AccessDeniedException("Active account required");
    
        }}

    void adminOrOrganizer(UUID actor, UUID organizationId) {
        if (identities.isActiveAdmin(actor)) {
            return;
        
        }if (!identities.isActiveOrganizer(actor) || !organizations.hasAuthority(organizationId, actor, "MANAGE_PROFILE")) {
            throw new AccessDeniedException("Organization authority required");
    
        }}

    void draftResource(UUID actor, UUID resourceId) {
        learning.requireDraftManage(actor, resourceId);
    }

    void readResource(UUID actor, UUID resourceId) {
        learning.requireRead(actor, resourceId);
    }
}
