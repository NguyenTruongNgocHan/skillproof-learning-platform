package com.skillproof.backend.organization.application;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.skillproof.backend.organization.contract.OrganizationPublicQuery;
import com.skillproof.backend.organization.infrastructure.OrganizationRepository;

@Service
public class OrganizationPublicQueryService implements OrganizationPublicQuery {

    private final OrganizationRepository organizations;

    public OrganizationPublicQueryService(OrganizationRepository organizations) {
        this.organizations = organizations;
    }

    @Override
    public Optional<OrganizationView> find(UUID organizationId) {
        return organizations.publicView(organizationId)
                .map(organization -> new OrganizationView(
                organization.id(), organization.displayName(), organization.status().name()));
    }
}
