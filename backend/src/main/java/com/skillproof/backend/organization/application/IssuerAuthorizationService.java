package com.skillproof.backend.organization.application;

import com.skillproof.backend.common.exception.*;
import com.skillproof.backend.identity.contract.IdentityAccessQuery;
import com.skillproof.backend.operations.contract.ActivityJournal;
import com.skillproof.backend.organization.contract.*;
import com.skillproof.backend.organization.infrastructure.*;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;

@Service
public class IssuerAuthorizationService implements IssuerAuthorizationQuery {
    @org.springframework.beans.factory.annotation.Autowired
    private com.skillproof.backend.course.contract.CourseWorkspaceQuery courses;

    @jakarta.persistence.PersistenceContext private jakarta.persistence.EntityManager entityManager;
    private final IssuerApplicationRepository applications;
    private final EvidenceRepository evidence;
    private final OrganizationRepository organizations;
    private final OrganizationPublicQuery publicQuery;
    private final OrganizationEvidenceService access;
    private final OrganizationAuthorityQuery grants;
    private final IdentityAccessQuery identity;
    private final ActivityJournal journal;

    public IssuerAuthorizationService(
            IssuerApplicationRepository applications,
            EvidenceRepository evidence,
            OrganizationRepository organizations,
            OrganizationPublicQuery publicQuery,
            OrganizationEvidenceService access,
            OrganizationAuthorityQuery grants,
            IdentityAccessQuery identity,
            ActivityJournal journal) {
        this.applications = applications;
        this.evidence = evidence;
        this.organizations = organizations;
        this.publicQuery = publicQuery;
        this.access = access;
        this.grants = grants;
        this.identity = identity;
        this.journal = journal;
    }

    @Transactional(readOnly = true)
    public List<IssuerApplication> list(UUID actor, UUID org) {
        access.requireRead(actor, org);
        return applications.findByOrganizationIdOrderByCreatedAtDesc(org);
    }

    public List<IssuerApplication> pending(UUID actor) {
        if (!identity.isActiveAdmin(actor)) throw new AccessDeniedException("Admin required");
        return applications.findByStatusOrderByCreatedAtAsc("PENDING");
    }

    @Transactional
    public IssuerApplication apply(
            UUID actor,
            UUID org,
            String scope,
            List<UUID> ids,
            String scopeMode,
            List<UUID> versionIds) {
        publicQuery.requireApprovedForUpdate(org);
        if (!grants.hasAuthority(org, actor, "MANAGE_PROFILE"))
            throw new AccessDeniedException("Profile authority required");
        if (applications.findByOrganizationIdOrderByCreatedAtDesc(org).stream()
                .anyMatch(
                        a ->
                                "PENDING".equals(a.getStatus())
                                        || ("AUTHORIZED".equals(a.getStatus())
                                                && a.getValidUntil()
                                                        .isAfter(
                                                                Instant.now()
                                                                        .plus(
                                                                                Duration.ofDays(
                                                                                        30))))))
            throw new ConflictException(
                    "ISSUER_APPLICATION_EXISTS",
                    "Only one pending request is allowed; renew an active authorization during its"
                            + " final 30 days");
        var selected =
                evidence.findByOrganizationIdOrderByCreatedAtDesc(org).stream()
                        .filter(
                                d ->
                                        ids.contains(d.getId())
                                                && "ISSUER".equals(d.getPurpose())
                                                && (d.getExpiresAt() == null
                                                        || d.getExpiresAt().isAfter(Instant.now())))
                        .toList();
        if (selected.size() != ids.size()
                || selected.stream().noneMatch(d -> "TRAINING_CAPABILITY".equals(d.getKind())))
            throw new BadRequestException("ISSUER_EVIDENCE", "Attach training capability evidence");
        if (!List.of("ALL_COURSES", "SELECTED_VERSIONS").contains(scopeMode)
                || ("SELECTED_VERSIONS".equals(scopeMode)
                        && (versionIds == null || versionIds.isEmpty())))
            throw new BadRequestException(
                    "ISSUER_SCOPE", "Select approved courses or all courses scope");
        if (versionIds != null)
            for (UUID version : versionIds) {
                var c = courses.context(version);
                if (!org.equals(c.organizationId()) || !"PUBLISHED".equals(c.status()))
                    throw new BadRequestException(
                            "ISSUER_SCOPE",
                            "Scope must contain published versions owned by this organization");
            }
        var a =
                applications.save(
                        new IssuerApplication(
                                UUID.randomUUID(),
                                org,
                                "PENDING",
                                scope,
                                ids.stream()
                                        .map(UUID::toString)
                                        .collect(java.util.stream.Collectors.joining(",")),
                                null,
                                null,
                                Instant.now(),
                                null,
                                null));
        a.restrictScope(
                scopeMode,
                versionIds == null
                        ? ""
                        : versionIds.stream()
                                .distinct()
                                .map(UUID::toString)
                                .collect(java.util.stream.Collectors.joining(",")));
        journal.record(actor, org, "ISSUER_APPLY", a.getId(), scope);
        return a;
    }

    @Transactional
    public IssuerApplication decide(
            UUID actor, UUID id, String status, String reason, Instant expiry) {
        if (!identity.isActiveAdmin(actor)) throw new AccessDeniedException("Admin required");
        var a = applications.findById(id).orElseThrow();
        organizations.lockApplication(a.getOrganizationId()).orElseThrow();
        entityManager.refresh(a, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
        if (!List.of("AUTHORIZED", "REJECTED", "SUSPENDED", "REVOKED").contains(status))
            throw new BadRequestException("ISSUER_STATUS", "Invalid decision");
        if (List.of("AUTHORIZED", "REJECTED").contains(status) && !"PENDING".equals(a.getStatus()))
            throw new ConflictException("ISSUER_STATE", "Review a pending application");
        if (List.of("SUSPENDED", "REVOKED").contains(status) && !"AUTHORIZED".equals(a.getStatus()))
            throw new ConflictException(
                    "ISSUER_STATE", "Only active issuer authorization can be suspended or revoked");
        if ("AUTHORIZED".equals(status)) {
            if (expiry == null || !expiry.isAfter(Instant.now()))
                throw new BadRequestException(
                        "ISSUER_EXPIRY", "Future authorization expiry required");
            publicQuery.requireApprovedForUpdate(a.getOrganizationId());
            var ids = Arrays.stream(a.getEvidenceIds().split(",")).map(UUID::fromString).toList();
            var documents = evidence.findAllById(ids);
            if (documents.size() != ids.size())
                throw new BadRequestException("ISSUER_EVIDENCE_REVIEW", "Evidence is missing");
            for (var doc : documents) {
                if (!"ACCEPTED".equals(doc.getStatus())
                        || (doc.getExpiresAt() != null && doc.getExpiresAt().isBefore(expiry)))
                    throw new BadRequestException(
                            "ISSUER_EVIDENCE_REVIEW",
                            "Evidence must be accepted and valid for the authorization period");
            }
        }
        if ("AUTHORIZED".equals(status)) {
            for (var previous :
                    applications.findByOrganizationIdOrderByCreatedAtDesc(a.getOrganizationId())) {
                if (!previous.getId().equals(id) && "AUTHORIZED".equals(previous.getStatus())) {
                    previous.decide(
                            "REVOKED",
                            "Superseded by authorization " + id,
                            actor,
                            previous.getValidUntil());
                    journal.record(
                            actor,
                            a.getOrganizationId(),
                            "ISSUER_SUPERSEDED",
                            previous.getId(),
                            id.toString());
                }
            }
        }
        a.decide(status, reason, actor, "AUTHORIZED".equals(status) ? expiry : a.getValidUntil());
        journal.record(actor, a.getOrganizationId(), "ISSUER_" + status, a.getId(), reason);
        journal.notify(
                organizations.publicView(a.getOrganizationId()).orElseThrow().ownerUserId(),
                a.getOrganizationId(),
                "ISSUER",
                status + ": " + reason,
                "/organizer/governance");
        return a;
    }

    @Override
    public Authorization authorization(UUID org, UUID version) {
        publicQuery.requireApprovedForUpdate(org);
        requireAuthorized(org);
        var a = active(org);
        if ("SELECTED_VERSIONS".equals(a.getScopeMode())
                && !Arrays.asList(a.getCourseVersionIds().split(",")).contains(version.toString()))
            throw new AccessDeniedException("Course version is outside authorized issuer scope");
        return new Authorization(
                a.getId(), org, a.getScope(), a.getEvidenceIds(), a.getValidUntil());
    }

    private IssuerApplication active(UUID org) {
        return applications.findByOrganizationIdOrderByCreatedAtDesc(org).stream()
                .filter(
                        a ->
                                "AUTHORIZED".equals(a.getStatus())
                                        && a.getValidUntil() != null
                                        && a.getValidUntil().isAfter(Instant.now()))
                .findFirst()
                .orElseThrow(
                        () ->
                                new AccessDeniedException(
                                        "Issuer authorization inactive or expired"));
    }

    @Override
    public void requireAuthorized(UUID org) {
        active(org);
    }
}
