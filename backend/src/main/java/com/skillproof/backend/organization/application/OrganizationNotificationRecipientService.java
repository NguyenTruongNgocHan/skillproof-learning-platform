package com.skillproof.backend.organization.application;

import com.skillproof.backend.organization.contract.OrganizationNotificationRecipients;
import com.skillproof.backend.organization.infrastructure.OrganizationRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class OrganizationNotificationRecipientService
        implements OrganizationNotificationRecipients {
    private final OrganizationRepository organizations;

    public OrganizationNotificationRecipientService(OrganizationRepository organizations) {
        this.organizations = organizations;
    }

    @Transactional(readOnly = true)
    public List<UUID> forAuthority(UUID org, String authority) {
        return organizations.members(org).stream()
                .filter(m -> m.active() && organizations.hasGrant(org, m.userId(), authority))
                .map(m -> m.userId())
                .distinct()
                .toList();
    }
}
