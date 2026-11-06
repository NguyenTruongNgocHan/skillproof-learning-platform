package com.skillproof.backend.organization.infrastructure;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Immutable classification submitted with one organization application revision. */
@Entity
@Table(
    name = "organization_revision_evidence",
    uniqueConstraints = @UniqueConstraint(columnNames = {"revision_id", "evidence_id"}))
public class ApplicationEvidenceSnapshot {
  @Id private UUID id;

  @Column(name = "revision_id", nullable = false, updatable = false)
  private UUID revisionId;

  @Column(name = "evidence_id", nullable = false, updatable = false)
  private UUID evidenceId;

  @Column(name = "media_id", nullable = false, updatable = false)
  private UUID mediaId;

  @Column(nullable = false, updatable = false, length = 40)
  private String kind;

  @Column(nullable = false, updatable = false, length = 180)
  private String title;

  @Column(name = "expires_at", updatable = false)
  private Instant expiresAt;

  @Column(nullable = false, updatable = false, length = 64)
  private String sha256;

  public String getSha256() {
    return sha256;
  }

  @Column(nullable = false, length = 20)
  private String status = "UPLOADED";

  @Column(length = 1000)
  private String reason;

  @Column(name = "reviewer_id")
  private UUID reviewerId;

  @Column(name = "reviewed_at")
  private Instant reviewedAt;

  public String getReason() {
    return reason;
  }

  public UUID getReviewerId() {
    return reviewerId;
  }

  public Instant getReviewedAt() {
    return reviewedAt;
  }

  public String getStatus() {
    return status;
  }

  public void review(String status, String reason, UUID reviewer) {
    this.status = status;
    this.reason = reason;
    reviewerId = reviewer;
    reviewedAt = Instant.now();
  }

  protected ApplicationEvidenceSnapshot() {}

  public ApplicationEvidenceSnapshot(UUID revision, EvidenceDocument d, String fingerprint) {
    id = UUID.randomUUID();
    revisionId = revision;
    evidenceId = d.getId();
    mediaId = d.getMediaId();
    kind = d.getKind();
    title = d.getTitle();
    expiresAt = d.getExpiresAt();
    sha256 = fingerprint;
  }

  public UUID getEvidenceId() {
    return evidenceId;
  }

  public UUID getMediaId() {
    return mediaId;
  }

  public String getKind() {
    return kind;
  }

  public String getTitle() {
    return title;
  }

  public Instant getExpiresAt() {
    return expiresAt;
  }
}
