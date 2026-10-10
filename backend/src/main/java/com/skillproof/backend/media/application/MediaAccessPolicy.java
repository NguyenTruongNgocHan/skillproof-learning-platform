package com.skillproof.backend.media.application;

import com.skillproof.backend.identity.contract.IdentityAccessQuery;
import com.skillproof.backend.course.contract.CourseResourceAccessQuery;
import com.skillproof.backend.organization.contract.OrganizationApplicationAccessQuery;
import com.skillproof.backend.organization.contract.OrganizationAuthorityQuery;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

/**
 * Media owns storage authorization orchestration; Course remains authority for
 * learning-resource access.
 */
@Component
public class MediaAccessPolicy {

    private final IdentityAccessQuery identities;
    private final CourseResourceAccessQuery learning;
    private final OrganizationAuthorityQuery organizations;
    private final OrganizationApplicationAccessQuery organizationView;

    public MediaAccessPolicy(
            IdentityAccessQuery identities,
            CourseResourceAccessQuery learning,
            OrganizationAuthorityQuery organizations,
            OrganizationApplicationAccessQuery organizationView
    ) {
        this.identities = identities;
        this.learning = learning;
        this.organizations = organizations;
        this.organizationView = organizationView;
    }

    public void active(UUID actor) {
        if (identities
                .find(actor)
                .filter(IdentityAccessQuery.Account::active)
                .isEmpty()) {
            throw new AccessDeniedException("Active account required");
        }
    }

    public void adminOrOrganizer(UUID actor, UUID organizationId) {
        if (identities.isActiveAdmin(actor)) {
            return;
        }
        if (!identities.isActiveOrganizer(actor)
                || (!organizations.hasAuthority(
                        organizationId,
                        actor,
                        "MANAGE_PROFILE"
                )
                && !organizationView.isOwner(organizationId, actor))) {
            throw new AccessDeniedException("Organization authority required");
        }
    }

    public void writeOrganization(UUID actor, UUID organizationId) {
        if (!identities.isActiveOrganizer(actor)
                || !organizationView.lockEditableApplication(organizationId, actor)) {
            throw new AccessDeniedException(
                    "Only the owner may edit draft application documents"
            );
        }
    }

    public void removeOrganizationDocument(
            UUID actor,
            UUID organizationId,
            UUID mediaId
    ) {
        writeOrganization(actor, organizationId);
    }

    public boolean retainOrganizationDocument(UUID organizationId, UUID mediaId) {
        return organizationView.documentInSubmission(organizationId, mediaId);
    }

    public void draftResource(UUID actor, UUID resourceId) {
        learning.requireDraftManage(actor, resourceId);
    }

    public void readResource(UUID actor, UUID resourceId) {
        learning.requireRead(actor, resourceId);
    }
}
