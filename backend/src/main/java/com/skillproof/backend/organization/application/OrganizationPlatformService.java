package com.skillproof.backend.organization.application;

import com.skillproof.backend.identity.contract.IdentityAccessQuery;
import com.skillproof.backend.operations.contract.ActivityJournal;
import com.skillproof.backend.organization.domain.Organization;
import com.skillproof.backend.organization.infrastructure.OrganizationRepository;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class OrganizationPlatformService {
    private final OrganizationRepository organizations;
    private final IdentityAccessQuery identity;
    private final ActivityJournal journal;

    public OrganizationPlatformService(
            OrganizationRepository organizations,
            IdentityAccessQuery identity,
            ActivityJournal journal) {
        this.organizations = organizations;
        this.identity = identity;
        this.journal = journal;
    }

    public Organization get(UUID actor, UUID id) {
        if (!identity.isActiveAdmin(actor)) throw new AccessDeniedException("Admin required");
        return organizations.find(id).orElseThrow();
    }

    @Transactional
    public Organization status(UUID actor, UUID id, boolean suspended, String reason) {
        if (!identity.isActiveAdmin(actor)) throw new AccessDeniedException("Admin required");
        var org = organizations.platformStatus(id, suspended, reason);
        journal.record(
                actor,
                id,
                suspended ? "ORGANIZATION_SUSPENDED" : "ORGANIZATION_RESTORED",
                id,
                reason);
        journal.notify(
                org.ownerUserId(), id, "ORGANIZATION", reason, "/organizer/verification-pending");
        return org;
    }
}
