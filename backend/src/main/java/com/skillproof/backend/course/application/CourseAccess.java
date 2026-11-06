package com.skillproof.backend.course.application;

import com.skillproof.backend.course.infrastructure.CourseEntity;
import com.skillproof.backend.identity.contract.IdentityAccessQuery;
import com.skillproof.backend.organization.contract.OrganizationAuthorityQuery;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

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
    if (organization == null
        || !identities.isActiveOrganizer(actor)
        || !organizations.hasAuthority(organization, actor, "MANAGE_CONTENT")) {
      throw new AccessDeniedException("Approved organization content authority required");
    }
  }

  public void manage(UUID actor, CourseEntity course) {
    if (course.getOrganizationId() != null) {
      organizer(actor, course.getOrganizationId());
    } else {
      learner(actor);
      if (!actor.equals(course.getCreatedBy())) {
        throw new AccessDeniedException("Course author required");
      }
    }
  }

  public void managePricing(UUID actor, CourseEntity course) {
    if (course.getOrganizationId() == null) {
      manage(actor, course);
      return;
    }
    if (!identities.isActiveOrganizer(actor)
        || !organizations.hasAuthority(course.getOrganizationId(), actor, "MANAGE_PRICING")) {
      throw new AccessDeniedException("Pricing authority required");
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
