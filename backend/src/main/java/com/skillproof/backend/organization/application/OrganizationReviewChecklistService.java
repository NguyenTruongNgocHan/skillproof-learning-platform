package com.skillproof.backend.organization.application;

import com.skillproof.backend.organization.infrastructure.OrganizationReviewChecklistRepository;
import com.skillproof.backend.organization.infrastructure.OrganizationReviewChecklistRepository.Item;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class OrganizationReviewChecklistService {

  private static final Set<String> KEYS =
      Set.of(
          "legal_identity",
          "representative",
          "expiry",
          "company_images",
          "evidence_decisions",
          "final_consistency");
  private final OrganizationReviewChecklistRepository repository;
  private final OrganizationService organizations;

  public OrganizationReviewChecklistService(
      OrganizationReviewChecklistRepository repository, OrganizationService organizations) {
    this.repository = repository;
    this.organizations = organizations;
  }

  public record Session(
      String submissionId,
      String reviewType,
      String reviewerId,
      Instant updatedAt,
      List<Item> items) {}

  public record Summary(int organizationPending, long organizationOverdue, Instant generatedAt) {}

  private void requireRevision(UUID revisionId) {
    if (!repository.revisionExists(revisionId)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Revision not found");
    }
  }

  public Session load(UUID revisionId, UUID reviewerId) {
    requireRevision(revisionId);
    return new Session(
        revisionId.toString(),
        "ORGANIZATION",
        reviewerId.toString(),
        repository.updatedAt(revisionId, reviewerId),
        repository.load(revisionId, reviewerId));
  }

  @Transactional
  public Session save(UUID revisionId, UUID reviewerId, List<Item> items) {
    requireRevision(revisionId);
    if (items == null || items.size() > KEYS.size()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid checklist");
    }
    Set<String> seen = new HashSet<>();
    for (Item item : items) {
      if (item == null
          || !KEYS.contains(item.key())
          || !seen.add(item.key())
          || (item.note() != null && item.note().length() > 1000)) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid checklist item");
      }
    }
    for (Item item : items) {
      repository.save(revisionId, reviewerId, item);
    }
    return load(revisionId, reviewerId);
  }

  public Summary summary() {
    return new Summary(organizations.pending().size(), repository.overdueCount(), Instant.now());
  }

  public record Analytics(
      List<OrganizationReviewChecklistRepository.DailyReviewVolume> daily,
      List<OrganizationReviewChecklistRepository.AgeBucket> ageBuckets) {}

  public Analytics analytics() {
    return new Analytics(repository.dailyReviewVolume(), repository.pendingAgeBuckets());
  }
}
