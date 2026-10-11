package com.skillproof.backend.operations.application;

import com.skillproof.backend.operations.infrastructure.*;
import com.skillproof.backend.organization.contract.OrganizationAuthorityQuery;

import org.springframework.data.domain.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class WorkspaceActivityService {
    @org.springframework.beans.factory.annotation.Autowired
    private com.skillproof.backend.identity.contract.IdentityAccessQuery identity;

    private final AuditRepository audit;
    private final NoticeRepository notices;
    private final OrganizationAuthorityQuery authority;

    public WorkspaceActivityService(
            AuditRepository audit, NoticeRepository notices, OrganizationAuthorityQuery authority) {
        this.audit = audit;
        this.notices = notices;
        this.authority = authority;
    }

    @Transactional(readOnly = true)
    public Page<AuditEntry> audit(UUID actor, UUID org, int page) {
        if (!identity.isActiveAdmin(actor) && !authority.hasAuthority(org, actor, "MANAGE_MEMBERS"))
            throw new AccessDeniedException("Team authority required");
        return audit.findByOrganizationIdOrderByCreatedAtDesc(
                org, PageRequest.of(Math.max(0, page), 30));
    }

    @Transactional(readOnly = true)
    public Page<WorkspaceNotice> notices(UUID actor, int page) {
        return notices.findByRecipientIdOrderByCreatedAtDesc(
                actor, PageRequest.of(Math.max(0, page), 30));
    }

    @Transactional(readOnly = true)
    public long unread(UUID actor) {
        return notices.countByRecipientIdAndReadAtIsNull(actor);
    }

    @Transactional
    public void read(UUID actor, UUID id) {
        var n = notices.findById(id).orElseThrow();
        if (!n.getRecipientId().equals(actor))
            throw new AccessDeniedException("Recipient required");
        n.markRead();
    }
}
