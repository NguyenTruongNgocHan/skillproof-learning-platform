package com.skillproof.backend.organization.application;

import com.skillproof.backend.organization.contract.OrganizationApplicationAccessQuery;
import com.skillproof.backend.organization.domain.Organization;
import com.skillproof.backend.organization.infrastructure.OrganizationRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrganizationApplicationAccessService implements OrganizationApplicationAccessQuery {

  @org.springframework.beans.factory.annotation.Autowired
  private com.skillproof.backend.organization.infrastructure.EvidenceRepository evidence;

  private final OrganizationRepository organizations;

  public OrganizationApplicationAccessService(OrganizationRepository organizations) {
    this.organizations = organizations;
  }

  public boolean isOwner(UUID organizationId, UUID actorId) {
    return organizations
        .publicView(organizationId)
        .filter(o -> o.ownerUserId().equals(actorId))
        .isPresent();
  }

  @Transactional(propagation = Propagation.MANDATORY)
  public boolean lockEditableApplication(UUID organizationId, UUID actorId) {
    return organizations
        .lockApplication(organizationId)
        .filter(
            o ->
                o.ownerUserId().equals(actorId)
                    && (o.status() == Organization.Status.DRAFT
                        || o.status() == Organization.Status.REJECTED
                        || o.status() == Organization.Status.NEEDS_INFORMATION))
        .isPresent();
  }

  public boolean documentInSubmission(UUID organizationId, UUID mediaId) {
    return evidence.existsByOrganizationIdAndMediaId(organizationId, mediaId)
        || organizations.applicationRevisions(organizationId).stream()
            .anyMatch(r -> r.getDocumentMediaIds().contains(mediaId));
  }
}
