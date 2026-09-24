package com.skillproof.backend.organization.application;

import com.skillproof.backend.common.exception.BadRequestException;
import com.skillproof.backend.common.exception.ConflictException;
import com.skillproof.backend.common.exception.NotFoundException;
import com.skillproof.backend.identity.domain.AccountStatus;
import com.skillproof.backend.identity.domain.UserRole;
import com.skillproof.backend.identity.infrastructure.UserAccountRepository;
import com.skillproof.backend.organization.domain.Organization;
import com.skillproof.backend.organization.infrastructure.OrganizationRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class OrganizationService {

    private final OrganizationRepository organizations;
    private final UserAccountRepository users;

    public OrganizationService(OrganizationRepository organizations, UserAccountRepository users) {
        this.organizations = organizations;
        this.users = users;
    }

    private void organizer(UUID actor) {
        var u = users.findById(actor).orElseThrow(() -> new AccessDeniedException("Account unavailable"));
        if (u.getRole() != UserRole.ORGANIZER || u.getStatus() != AccountStatus.ACTIVE) {
            throw new AccessDeniedException("Active organizer required");
    
        }}

    private Organization find(UUID id) {
        return organizations.find(id).orElseThrow(() -> new NotFoundException("ORGANIZATION_NOT_FOUND", "Organization not found"));
    }

    private void allow(UUID id, UUID actor, String authority) {
        organizer(actor);
        if (!organizations.hasGrant(id, actor, authority)) {
            throw new AccessDeniedException("Organization authority required");
    
        }}

    @Transactional
    public Organization create(UUID actor, Create input) {
        organizer(actor);
        if (organizations.owned(actor).isPresent()) {
            throw new ConflictException("ORGANIZATION_ALREADY_EXISTS", "This organizer already has an organization application");
        
        }Instant now = Instant.now();
        var o = new Organization(UUID.randomUUID(), actor, input.legalName().trim(), input.displayName().trim(), input.website(), input.industry().trim(), input.country().trim(), input.registrationNumber(), input.contactName().trim(), input.contactEmail().trim(), input.contactPhone(), Organization.Status.PENDING, null, now, now);
        try {
            organizations.create(o);
            organizations.addMember(o.id(), actor, actor, now, true);
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("ORGANIZATION_ALREADY_EXISTS", "An organization application already exists");
        }
        return o;
    }

    @Transactional
    public Organization resubmit(UUID actor, Create input) {
        organizer(actor);
        Organization previous = organizations.owned(actor).orElseThrow(() -> new NotFoundException("ORGANIZATION_NOT_FOUND", "No organization application found"));
        Instant now = Instant.now();
        Organization replacement = new Organization(previous.id(), actor, input.legalName().trim(), input.displayName().trim(), input.website(), input.industry().trim(), input.country().trim(), input.registrationNumber(), input.contactName().trim(), input.contactEmail().trim(), input.contactPhone(), Organization.Status.PENDING, null, previous.createdAt(), now);
        if (organizations.resubmit(previous.id(), replacement, now) != 1) {
            throw new ConflictException("REVIEW_STATE_INVALID", "Only a rejected application can be resubmitted");
        
        }return find(previous.id());
    }

    public Organization mine(UUID actor) {
        organizer(actor);
        return organizations.owned(actor).or(() -> organizations.memberOrganization(actor)).orElseThrow(() -> new NotFoundException("ORGANIZATION_NOT_FOUND", "No organization application found"));
    }

    public Organization get(UUID id, UUID actor, boolean admin) {
        Organization o = find(id);
        if (!admin && !organizations.activeMember(id, actor)) {
            throw new AccessDeniedException("Organization membership required");
        
        }return o;
    }

    public List<Map<String, Object>> reviews(UUID id) {
        find(id);
        return organizations.reviews(id);
    }

    public List<Organization> pending() {
        return organizations.pending();
    }

    @Transactional
    public Organization review(UUID id, UUID actor, Review input) {
        var decision = input.decision();
        if (decision == Organization.Status.PENDING) {
            throw new BadRequestException("INVALID_DECISION", "A final decision is required");
        
        }if (decision == Organization.Status.REJECTED && (input.reason() == null || input.reason().isBlank())) {
            throw new BadRequestException("REASON_REQUIRED", "Provide a rejection reason");
        
        }find(id);
        Instant now = Instant.now();
        if (organizations.review(id, decision, input.reason(), now) != 1) {
            throw new ConflictException("REVIEW_ALREADY_DECIDED", "This application has already been reviewed");
        
        }organizations.logReview(id, actor, decision, input.reason(), now);
        return find(id);
    }

    @Transactional
    public Organization update(UUID id, UUID actor, Update input) {
        allow(id, actor, "MANAGE_PROFILE");
        organizations.update(id, input.displayName().trim(), input.website(), input.industry().trim(), input.contactPhone(), Instant.now());
        return find(id);
    }

    public List<Map<String, Object>> members(UUID id, UUID actor) {
        allow(id, actor, "MANAGE_MEMBERS");
        var rows = organizations.members(id);
        return rows.stream().map(row -> {
            var entry = new java.util.LinkedHashMap<String, Object>(row);
            entry.put("grants", organizations.grants(id, (UUID) row.get("user_id")));
            return (Map<String, Object>) entry;
        }).toList();
    }

    @Transactional
    public void addMember(UUID id, UUID actor, UUID user) {
        allow(id, actor, "MANAGE_MEMBERS");
        var candidate = users.findById(user).orElseThrow(() -> new NotFoundException("USER_NOT_FOUND", "Account not found"));
        if (candidate.getRole() != UserRole.ORGANIZER || candidate.getStatus() != AccountStatus.ACTIVE) {
            throw new BadRequestException("MEMBER_NOT_ELIGIBLE", "Active organizer required");
        
        }try {
            organizations.addMember(id, user, actor, Instant.now(), false);
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("MEMBER_EXISTS", "Member already exists");
        }
    }

    @Transactional
    public void setGrant(UUID id, UUID actor, UUID member, Grant input) {
        allow(id, actor, "MANAGE_MEMBERS");
        if (!organizations.activeMember(id, member)) {
            throw new NotFoundException("MEMBER_NOT_FOUND", "Active member not found");
        
        }if (find(id).ownerUserId().equals(member) && !input.active()) {
            throw new BadRequestException("OWNER_PROTECTED", "Owner authority cannot be revoked");
        
        }organizations.grant(id, member, input.authority().name(), actor, input.active(), Instant.now());
    }

    @Transactional
    public void removeMember(UUID id, UUID actor, UUID member) {
        allow(id, actor, "MANAGE_MEMBERS");
        if (find(id).ownerUserId().equals(member)) {
            throw new BadRequestException("OWNER_PROTECTED", "Owner cannot be removed");
        
        }if (organizations.deactivate(id, member) == 0) {
            throw new NotFoundException("MEMBER_NOT_FOUND", "Member not found");
    
        }}

    public boolean can(UUID id, UUID actor, String authority) {
        return organizations.hasGrant(id, actor, authority);
    }

    public record Create(String legalName, String displayName, String website, String industry, String country, String registrationNumber, String contactName, String contactEmail, String contactPhone) {

    }

    public record Update(String displayName, String website, String industry, String contactPhone) {

    }

    public record Review(Organization.Status decision, String reason) {

    }

    public enum Authority {
        MANAGE_PROFILE, MANAGE_MEMBERS, MANAGE_CONTENT, ISSUE_CERTIFICATES
    }

    public record Grant(Authority authority, boolean active) {

    }
}
