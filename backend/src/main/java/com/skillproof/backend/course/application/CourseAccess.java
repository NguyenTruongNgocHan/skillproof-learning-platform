package com.skillproof.backend.course.application;

import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import com.skillproof.backend.course.infrastructure.CourseEntity;
import com.skillproof.backend.identity.contract.IdentityAccessQuery;
import com.skillproof.backend.organization.contract.OrganizationAuthorityQuery;

@Component
public class CourseAccess {

    private final IdentityAccessQuery identities;
    private final OrganizationAuthorityQuery organizations;

    public CourseAccess(IdentityAccessQuery identities, OrganizationAuthorityQuery organizations) {
        this.identities = identities;
        this.organizations = organizations;
    }

    public void learner(UUID actor) {
        if (!identities.isActiveLearner(actor)) {
            throw new AccessDeniedException("Active learner required");
        }
    }

    public void organizer(UUID actor, UUID organization) {
        if (organization == null || !identities.isActiveOrganizer(actor)
                || !organizations.hasAuthority(organization, actor, "MANAGE_CONTENT")) {
            throw new AccessDeniedException("Approved organization content authority required");
        }
    }

    public void manage(UUID actor, CourseEntity course) {
        if (course.getOrganizationId() != null) {
            organizer(actor, course.getOrganizationId()); 
        }else {
            learner(actor);
            if (!actor.equals(course.getCreatedBy())) {
                throw new AccessDeniedException("Course author required");
            }
        }
    }

    public boolean canReview(UUID actor) {
        return identities.isActiveAdmin(actor);
    }

    public void admin(UUID actor) {
        if (!identities.isActiveAdmin(actor)) {
            throw new AccessDeniedException("Administrator required");
        }
    }
}
