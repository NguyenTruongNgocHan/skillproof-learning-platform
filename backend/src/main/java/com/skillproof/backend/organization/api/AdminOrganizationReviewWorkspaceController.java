package com.skillproof.backend.organization.api;

import com.skillproof.backend.organization.application.OrganizationReviewChecklistService;
import com.skillproof.backend.organization.infrastructure.OrganizationReviewChecklistRepository.Item;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/organizations")
public class AdminOrganizationReviewWorkspaceController {

  private final OrganizationReviewChecklistService service;

  public AdminOrganizationReviewWorkspaceController(OrganizationReviewChecklistService service) {
    this.service = service;
  }

  public record Update(@NotNull @Size(max = 6) List<@Valid Item> items) {}

  @GetMapping("/review-summary")
  public OrganizationReviewChecklistService.Summary summary() {
    return service.summary();
  }

  @GetMapping("/application-revisions/{revisionId}/checklist")
  public OrganizationReviewChecklistService.Session checklist(
      Authentication auth, @PathVariable UUID revisionId) {
    return service.load(revisionId, (UUID) auth.getPrincipal());
  }

  @PutMapping("/application-revisions/{revisionId}/checklist")
  public OrganizationReviewChecklistService.Session save(
      Authentication auth, @PathVariable UUID revisionId, @RequestBody Update input) {
    return service.save(revisionId, (UUID) auth.getPrincipal(), input.items());
  }

  @GetMapping("/review-analytics")
  public OrganizationReviewChecklistService.Analytics analytics() {
    return service.analytics();
  }
}
