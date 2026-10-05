package com.skillproof.backend.media;

import com.skillproof.backend.identity.contract.IdentityAccessQuery;
import com.skillproof.backend.learning.contract.LearningResourceAccessQuery;
import com.skillproof.backend.organization.contract.OrganizationApplicationAccessQuery;
import com.skillproof.backend.organization.contract.OrganizationAuthorityQuery;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

/**
 * Media owns storage authorization orchestration; Learning remains authority
 * for learning-resource access.
 */
@Component
public class MediaAccessPolicy {

    private final IdentityAccessQuery identities;
    private final LearningResourceAccessQuery learning;
    private final OrganizationAuthorityQuery organizations;
    private final OrganizationApplicationAccessQuery organizationView;

    public MediaAccessPolicy(
        IdentityAccessQuery identities,
        LearningResourceAccessQuery learning,
        OrganizationAuthorityQuery organizations,
        OrganizationApplicationAccessQuery organizationView
    ) {
        this.identities = identities;
        this.learning = learning;
        this.organizations = organizations;
        this.organizationView = organizationView;
    }

    void active(UUID actor) {
        if (
            identities
                .find(actor)
                .filter(IdentityAccessQuery.Account::active)
                .isEmpty()
        ) {
            throw new AccessDeniedException("Active account required");
        }
    }

    void adminOrOrganizer(UUID actor, UUID organizationId) {
        if (identities.isActiveAdmin(actor)) {
            return;
        }
        if (
            !identities.isActiveOrganizer(actor) ||
            (!organizations.hasAuthority(
                organizationId,
                actor,
                "MANAGE_PROFILE"
            ) &&
                !organizationView.isOwner(organizationId, actor))
        ) {
            throw new AccessDeniedException("Organization authority required");
        }
    }

    void writeOrganization(UUID actor, UUID organizationId) {
        if (
            !identities.isActiveOrganizer(actor) ||
            !organizationView.lockEditableApplication(organizationId, actor)
        ) {
            throw new AccessDeniedException(
                "Only the owner may edit draft application documents"
            );
        }
    }

    void removeOrganizationDocument(
        UUID actor,
        UUID organizationId,
        UUID mediaId
    ) {
        writeOrganization(actor, organizationId);
    }

    boolean retainOrganizationDocument(UUID organizationId, UUID mediaId) {
        return organizationView.documentInSubmission(organizationId, mediaId);
    }

    void draftResource(UUID actor, UUID resourceId) {
        learning.requireDraftManage(actor, resourceId);
    }

    void readResource(UUID actor, UUID resourceId) {
        learning.requireRead(actor, resourceId);
    }
}
