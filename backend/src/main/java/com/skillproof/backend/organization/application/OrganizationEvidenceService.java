package com.skillproof.backend.organization.application;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillproof.backend.common.exception.BadRequestException;
import com.skillproof.backend.identity.contract.IdentityAccessQuery;
import com.skillproof.backend.media.contract.OrganizationEvidenceFiles;
import com.skillproof.backend.operations.contract.ActivityJournal;
import com.skillproof.backend.organization.contract.OrganizationAuthorityQuery;
import com.skillproof.backend.organization.infrastructure.EvidenceDocument;
import com.skillproof.backend.organization.infrastructure.EvidenceRepository;
import com.skillproof.backend.organization.infrastructure.OrganizationRepository;

@Service
public class OrganizationEvidenceService {
    private final EvidenceRepository evidence;
    private final OrganizationRepository organizations;
    private final OrganizationAuthorityQuery grants;
    private final IdentityAccessQuery identity;
    private final OrganizationEvidenceFiles files;
    private final ActivityJournal journal;

    public OrganizationEvidenceService(
            EvidenceRepository evidence,
            OrganizationRepository organizations,
            OrganizationAuthorityQuery grants,
            IdentityAccessQuery identity,
            OrganizationEvidenceFiles files,
            ActivityJournal journal) {
        this.evidence = evidence;
        this.organizations = organizations;
        this.grants = grants;
        this.identity = identity;
        this.files = files;
        this.journal = journal;
    }

    public void requireRead(UUID actor, UUID org) {
        if (identity.isActiveAdmin(actor)) return;
        if (!organizations.publicView(org).map(o -> o.ownerUserId().equals(actor)).orElse(false)
                && !grants.hasAuthority(org, actor, "MANAGE_PROFILE"))
            throw new AccessDeniedException("Profile authority required");
    }

    @Transactional(readOnly = true)
    public List<EvidenceDocument> list(UUID actor, UUID org) {
        requireRead(actor, org);
        return evidence.findByOrganizationIdOrderByCreatedAtDesc(org);
    }

    @Transactional
    public EvidenceDocument add(
            UUID actor,
            UUID org,
            UUID media,
            String kind,
            String purpose,
            String title,
            Instant expiry) {
        var o = organizations.lockApplication(org).orElseThrow();
        if ("VERIFICATION".equals(purpose)) {
            if (!o.ownerUserId().equals(actor)
                    || !(o.status()
                                    == com.skillproof.backend.organization.domain.Organization
                                            .Status.DRAFT
                            || o.status()
                                    == com.skillproof.backend.organization.domain.Organization
                                            .Status.REJECTED
                            || o.status()
                                    == com.skillproof.backend.organization.domain.Organization
                                            .Status.NEEDS_INFORMATION))
                throw new AccessDeniedException("Editable owned draft required");
        } else if (!grants.hasAuthority(org, actor, "MANAGE_PROFILE"))
            throw new AccessDeniedException("Profile authority required");
        if (expiry != null && !expiry.isAfter(Instant.now()))
            throw new BadRequestException("EVIDENCE_EXPIRED", "Document has expired");
        if ("COMPANY_IMAGE".equals(kind)) {
            if (!"VERIFICATION".equals(purpose))
                throw new BadRequestException("COMPANY_IMAGE_PURPOSE", "Company images are verification evidence");
            files.requireImage(org, media);
        } else files.requireFile(org, media, purpose);
        var doc =
                evidence.save(
                        new EvidenceDocument(
                                UUID.randomUUID(),
                                org,
                                media,
                                kind,
                                purpose,
                                title,
                                expiry,
                                "UPLOADED",
                                null,
                                null,
                                Instant.now(),
                                null));
        journal.record(actor, org, "EVIDENCE_CLASSIFIED", doc.getId(), kind);
        return doc;
    }

    @Transactional
    public EvidenceDocument review(UUID actor, UUID org, UUID id, String status, String reason) {
        if (!identity.isActiveAdmin(actor)) throw new AccessDeniedException("Admin required");
        organizations.lockApplication(org).orElseThrow();
        var doc = evidence.findById(id).orElseThrow();
        if (!org.equals(doc.getOrganizationId()))
            throw new AccessDeniedException("Organization mismatch");
        doc.review(status, reason, actor);
        journal.record(actor, org, "EVIDENCE_REVIEW", id, status + ": " + reason);
        return doc;
    }

    private void requireThreeCompanyImages(List<EvidenceDocument> docs, boolean approved) {
        long count = docs.stream()
                .filter(d -> "COMPANY_IMAGE".equals(d.getKind()))
                .filter(d -> !approved || "ACCEPTED".equals(d.getStatus()))
                .map(EvidenceDocument::getMediaId).distinct().count();
        if (count < 3)
            throw new BadRequestException(approved ? "COMPANY_IMAGES_REVIEW_REQUIRED" : "COMPANY_IMAGES_REQUIRED",
                    "At least three distinct company images are required" + (approved ? " and must be accepted by an admin" : " before submission"));
    }

    public void requireSubmission(UUID org, List<UUID> activeFiles) {
        var docs =
                evidence.findByOrganizationIdOrderByCreatedAtDesc(org).stream()
                        .filter(
                                d ->
                                        "VERIFICATION".equals(d.getPurpose())
                                                && activeFiles.contains(d.getMediaId())
                                                && (d.getExpiresAt() == null
                                                        || d.getExpiresAt().isAfter(Instant.now())))
                        .toList();
        requireThreeCompanyImages(docs, false);
        for (String kind : List.of("LEGAL_IDENTITY", "REPRESENTATIVE"))
            if (docs.stream().noneMatch(d -> kind.equals(d.getKind())))
                throw new BadRequestException(
                        "EVIDENCE_REQUIRED",
                        "Attach and classify " + kind + " evidence before submission");
    }

    public void requireApprovedEvidence(UUID org, List<UUID> snapshot) {
        var docs = evidence.findByOrganizationIdOrderByCreatedAtDesc(org).stream()
                .filter(d -> snapshot.contains(d.getMediaId())
                        && "VERIFICATION".equals(d.getPurpose())
                        && (d.getExpiresAt() == null || d.getExpiresAt().isAfter(Instant.now())))
                .toList();
        requireThreeCompanyImages(docs, true);
        for (String kind : List.of("LEGAL_IDENTITY", "REPRESENTATIVE"))
            if (docs.stream()
                    .noneMatch(
                            d ->
                                    snapshot.contains(d.getMediaId())
                                            && kind.equals(d.getKind())
                                            && "ACCEPTED".equals(d.getStatus())
                                            && (d.getExpiresAt() == null
                                                    || d.getExpiresAt().isAfter(Instant.now()))))
                throw new BadRequestException(
                        "EVIDENCE_REVIEW_REQUIRED",
                        "Accept unexpired " + kind + " evidence before approval");
    }
}
