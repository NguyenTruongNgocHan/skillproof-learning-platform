package com.skillproof.backend.organization.api;

import com.skillproof.backend.organization.application.*;
import com.skillproof.backend.organization.infrastructure.*;
import jakarta.validation.*;
import jakarta.validation.constraints.*;
import java.time.*;
import java.util.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class OrganizationGovernanceController {
  private final OrganizationEvidenceService evidence;
  private final IssuerAuthorizationService issuer;

  public OrganizationGovernanceController(
      OrganizationEvidenceService evidence, IssuerAuthorizationService issuer) {
    this.evidence = evidence;
    this.issuer = issuer;
  }

  public record EvidenceInput(
      @NotNull UUID mediaId,
      @Pattern(regexp = "LEGAL_IDENTITY|REPRESENTATIVE|TRAINING_CAPABILITY|SUPPORTING") @NotBlank
          String kind,
      @Pattern(regexp = "VERIFICATION|ISSUER") @NotBlank String purpose,
      @NotBlank @Size(max = 180) String title,
      Instant expiresAt) {}

  public record EvidenceDecision(
      @NotBlank @Pattern(regexp = "ACCEPTED|REJECTED|NEEDS_INFORMATION") String status,
      @NotBlank @Size(max = 1000) String reason) {}

  public record IssuerInput(
      @NotBlank @Size(max = 2000) String scope,
      @NotEmpty @Size(max = 20) List<@NotNull UUID> evidenceIds,
      @NotBlank @Pattern(regexp = "ALL_COURSES|SELECTED_VERSIONS") String scopeMode,
      @Size(max = 100) List<@NotNull UUID> courseVersionIds) {}

  public record IssuerDecision(
      @NotBlank @Pattern(regexp = "AUTHORIZED|REJECTED|SUSPENDED|REVOKED") String status,
      @NotBlank @Size(max = 1000) String reason,
      Instant validUntil) {}

  @GetMapping("/organizations/{org}/evidence")
  public List<EvidenceDocument> list(Authentication a, @PathVariable UUID org) {
    return evidence.list((UUID) a.getPrincipal(), org);
  }

  @PostMapping("/organizations/{org}/evidence")
  public EvidenceDocument add(
      Authentication a, @PathVariable UUID org, @Valid @RequestBody EvidenceInput i) {
    return evidence.add(
        (UUID) a.getPrincipal(), org, i.mediaId(), i.kind(), i.purpose(), i.title(), i.expiresAt());
  }

  @PostMapping("/admin/organizations/{org}/evidence/{id}/review")
  public EvidenceDocument review(
      Authentication a,
      @PathVariable UUID org,
      @PathVariable UUID id,
      @Valid @RequestBody EvidenceDecision i) {
    return evidence.review((UUID) a.getPrincipal(), org, id, i.status(), i.reason());
  }

  @GetMapping("/organizations/{org}/issuer-applications")
  public List<IssuerApplication> issuers(Authentication a, @PathVariable UUID org) {
    return issuer.list((UUID) a.getPrincipal(), org);
  }

  @PostMapping("/organizations/{org}/issuer-applications")
  public IssuerApplication apply(
      Authentication a, @PathVariable UUID org, @Valid @RequestBody IssuerInput i) {
    return issuer.apply(
        (UUID) a.getPrincipal(),
        org,
        i.scope(),
        i.evidenceIds(),
        i.scopeMode(),
        i.courseVersionIds());
  }

  @GetMapping("/admin/issuer-applications")
  public List<IssuerApplication> pending(Authentication a) {
    return issuer.pending((UUID) a.getPrincipal());
  }

  @PostMapping("/admin/issuer-applications/{id}/review")
  public IssuerApplication decide(
      Authentication a, @PathVariable UUID id, @Valid @RequestBody IssuerDecision i) {
    return issuer.decide((UUID) a.getPrincipal(), id, i.status(), i.reason(), i.validUntil());
  }
}
