package com.skillproof.backend.organization.application;

import com.skillproof.backend.common.exception.BadRequestException;
import com.skillproof.backend.identity.contract.IdentityAccessQuery;
import com.skillproof.backend.media.contract.OrganizationEvidenceFiles;
import com.skillproof.backend.operations.contract.ActivityJournal;
import com.skillproof.backend.organization.contract.OrganizationAuthorityQuery;
import com.skillproof.backend.organization.infrastructure.EvidenceDocument;
import com.skillproof.backend.organization.infrastructure.EvidenceRepository;
import com.skillproof.backend.organization.infrastructure.OrganizationRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrganizationEvidenceService {
  @org.springframework.beans.factory.annotation.Autowired
  private com.skillproof.backend.organization.infrastructure.ApplicationEvidenceSnapshotRepository
      snapshots;

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
      UUID actor, UUID org, UUID media, String kind, String purpose, String title, Instant expiry) {
    var o = organizations.lockApplication(org).orElseThrow();
    if (media == null
        || !java.util.Set.of("VERIFICATION", "ISSUER").contains(purpose)
        || !java.util.Set.of(
                "LEGAL_IDENTITY",
                "REPRESENTATIVE",
                "COMPANY_IMAGE",
                "SUPPORTING",
                "TRAINING_CAPABILITY")
            .contains(kind)
        || title == null
        || title.isBlank()
        || title.length() > 180)
      throw new BadRequestException(
          "EVIDENCE_INVALID", "Valid evidence classification and title required");
    if (evidence.findByOrganizationIdOrderByCreatedAtDesc(org).stream()
        .anyMatch(d -> d.getMediaId().equals(media) && d.getPurpose().equals(purpose)))
      throw new com.skillproof.backend.common.exception.ConflictException(
          "EVIDENCE_ALREADY_CLASSIFIED", "Each uploaded file has one classification per purpose");
    if ("VERIFICATION".equals(purpose)) {
      if (!o.ownerUserId().equals(actor)
          || !(o.status() == com.skillproof.backend.organization.domain.Organization.Status.DRAFT
              || o.status()
                  == com.skillproof.backend.organization.domain.Organization.Status.REJECTED
              || o.status()
                  == com.skillproof.backend.organization.domain.Organization.Status
                      .NEEDS_INFORMATION))
        throw new AccessDeniedException("Editable owned draft required");
    } else if (!grants.hasAuthority(org, actor, "MANAGE_PROFILE"))
      throw new AccessDeniedException("Profile authority required");
    if (expiry != null && !expiry.isAfter(Instant.now()))
      throw new BadRequestException("EVIDENCE_EXPIRED", "Document has expired");
    if ("COMPANY_IMAGE".equals(kind)) {
      if (!"VERIFICATION".equals(purpose))
        throw new BadRequestException(
            "COMPANY_IMAGE_PURPOSE", "Company images are verification evidence");
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
    if (!java.util.Set.of("ACCEPTED", "REJECTED", "NEEDS_INFORMATION").contains(status)
        || reason == null
        || reason.isBlank()
        || reason.length() > 1000)
      throw new BadRequestException("EVIDENCE_DECISION", "Valid decision and reason required");
    if ("VERIFICATION".equals(doc.getPurpose())) {
      var revision =
          organizations
              .latestRevision(org)
              .orElseThrow(
                  () ->
                      new com.skillproof.backend.common.exception.ConflictException(
                          "APPLICATION_REQUIRED", "Submit an application first"));
      var snapshot =
          snapshots.findByRevisionId(revision.getId()).stream()
              .filter(s -> s.getEvidenceId().equals(id))
              .findFirst();
      if (!"PENDING".equals(revision.getStatus()) || snapshot.isEmpty())
        throw new com.skillproof.backend.common.exception.ConflictException(
            "EVIDENCE_REVIEW_STATE", "Review evidence from the current pending revision");
      snapshot.orElseThrow().review(status, reason.trim(), actor);
    }
    doc.review(status, reason.trim(), actor);
    journal.record(actor, org, "EVIDENCE_REVIEW", id, status + ": " + reason);
    return doc;
  }

  private void requireThreeCompanyImages(List<EvidenceDocument> docs, boolean approved) {
    long count =
        docs.stream()
            .filter(d -> "COMPANY_IMAGE".equals(d.getKind()))
            .filter(d -> !approved || "ACCEPTED".equals(d.getStatus()))
            .map(d -> files.fingerprint(d.getOrganizationId(), d.getMediaId()))
            .distinct()
            .count();
    if (count < 3)
      throw new BadRequestException(
          approved ? "COMPANY_IMAGES_REVIEW_REQUIRED" : "COMPANY_IMAGES_REQUIRED",
          "At least three distinct company images are required"
              + (approved ? " and must be accepted by an admin" : " before submission"));
  }

  public void requireSubmission(UUID org, List<UUID> activeFiles) {
    var docs =
        evidence.findByOrganizationIdOrderByCreatedAtDesc(org).stream()
            .filter(
                d ->
                    "VERIFICATION".equals(d.getPurpose())
                        && activeFiles.contains(d.getMediaId())
                        && (d.getExpiresAt() == null || d.getExpiresAt().isAfter(Instant.now())))
            .toList();
    requireThreeCompanyImages(docs, false);
    for (String kind : List.of("LEGAL_IDENTITY", "REPRESENTATIVE"))
      if (docs.stream().noneMatch(d -> kind.equals(d.getKind())))
        throw new BadRequestException(
            "EVIDENCE_REQUIRED", "Attach and classify " + kind + " evidence before submission");
  }

  public record SnapshotView(
      UUID evidenceId,
      UUID mediaId,
      String kind,
      String title,
      Instant expiresAt,
      String status,
      String sha256,
      String reason,
      UUID reviewerId,
      Instant reviewedAt) {}

  public void capture(UUID revision, UUID org, List<UUID> activeFiles) {
    var rows =
        evidence.findByOrganizationIdOrderByCreatedAtDesc(org).stream()
            .filter(
                d -> "VERIFICATION".equals(d.getPurpose()) && activeFiles.contains(d.getMediaId()))
            .map(
                d ->
                    new com.skillproof.backend.organization.infrastructure
                        .ApplicationEvidenceSnapshot(
                        revision, d, files.fingerprint(org, d.getMediaId())))
            .toList();
    snapshots.saveAll(rows);
  }

  public List<SnapshotView> snapshot(UUID revision) {
    return snapshots.findByRevisionId(revision).stream()
        .map(
            s ->
                new SnapshotView(
                    s.getEvidenceId(),
                    s.getMediaId(),
                    s.getKind(),
                    s.getTitle(),
                    s.getExpiresAt(),
                    s.getStatus(),
                    s.getSha256(),
                    s.getReason(),
                    s.getReviewerId(),
                    s.getReviewedAt()))
        .toList();
  }

  public void requireApprovedSnapshot(UUID revision) {
    var docs =
        snapshot(revision).stream()
            .filter(
                d ->
                    "ACCEPTED".equals(d.status())
                        && (d.expiresAt() == null || d.expiresAt().isAfter(Instant.now())))
            .toList();
    long images =
        docs.stream()
            .filter(d -> "COMPANY_IMAGE".equals(d.kind()))
            .map(SnapshotView::sha256)
            .distinct()
            .count();
    if (images < 3)
      throw new BadRequestException(
          "COMPANY_IMAGES_REVIEW_REQUIRED",
          "Accept at least three distinct company images from this revision");
    for (String kind : List.of("LEGAL_IDENTITY", "REPRESENTATIVE"))
      if (docs.stream().noneMatch(d -> kind.equals(d.kind())))
        throw new BadRequestException(
            "EVIDENCE_REVIEW_REQUIRED",
            "Accept unexpired " + kind + " evidence from this revision");
  }
}
