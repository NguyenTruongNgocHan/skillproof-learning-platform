package com.skillproof.backend.organization.application;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.skillproof.backend.organization.contract.OrganizationPublicQuery;
import com.skillproof.backend.organization.infrastructure.OrganizationRepository;

@Service
public class OrganizationPublicQueryService implements OrganizationPublicQuery {

    private final OrganizationRepository organizations;

    public OrganizationPublicQueryService(
            OrganizationRepository organizations
    ) {
        this.organizations = organizations;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(propagation = org.springframework.transaction.annotation.Propagation.MANDATORY)
    public void requireApprovedForUpdate(UUID organizationId) {
        var organization = organizations.lockApplication(organizationId).orElseThrow(()
                -> new com.skillproof.backend.common.exception.NotFoundException("ORGANIZATION_NOT_FOUND", "Organization not found"));
        if (organization.status() != com.skillproof.backend.organization.domain.Organization.Status.APPROVED) {
            throw new com.skillproof.backend.common.exception.ConflictException("ORGANIZATION_NOT_APPROVED", "Organization must be approved");
        }
    }

    @Override
    public Optional<OrganizationView> find(UUID organizationId) {
        return organizations
                .publicView(organizationId)
                .map(organization
                        -> new OrganizationView(
                        organization.id(),
                        organization.displayName(),
                        organization.status().name()
                )
                );
    }
}
