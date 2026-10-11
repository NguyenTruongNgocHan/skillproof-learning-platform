package com.skillproof.backend.access.application;

import com.skillproof.backend.commerce.domain.ProductType;
import com.skillproof.backend.common.exception.BadRequestException;
import com.skillproof.backend.identity.contract.IdentityAccessQuery;
import java.time.Instant;
import java.util.*;
import org.springframework.stereotype.Service;

@Service
public class OrganizationAccessImportService {
  public record Row(int row, String identity, UUID learnerId, boolean eligible, String error) {}

  private final IdentityAccessQuery identity;
  private final AccessGrantService grants;

  public OrganizationAccessImportService(IdentityAccessQuery identity, AccessGrantService grants) {
    this.identity = identity;
    this.grants = grants;
  }

  public List<Row> preview(
      UUID actor, UUID org, List<String> entries, ProductType type, UUID product, Instant expiry) {
    grants.requireImport(actor, org, type, product, expiry);
    if (entries == null || entries.isEmpty() || entries.size() > 1000)
      throw new BadRequestException("IMPORT_ROWS", "Import 1–1000 rows");
    List<Row> rows = new ArrayList<>();
    Set<UUID> seen = new HashSet<>();
    for (int n = 0; n < entries.size(); n++) {
      String value = entries.get(n) == null ? "" : entries.get(n).trim();
      UUID learner = null;
      String error = "";
      try {
        learner =
            value.contains("@")
                ? identity
                    .findByEmail(value.toLowerCase(Locale.ROOT))
                    .filter(IdentityAccessQuery.Account::active)
                    .map(IdentityAccessQuery.Account::id)
                    .orElse(null)
                : UUID.fromString(value);
        if (learner == null || !identity.isActiveLearner(learner)) {
          error = "No eligible active learner";
          learner = null;
        } else if (!seen.add(learner)) error = "Duplicate learner in this import";
      } catch (IllegalArgumentException ex) {
        error = "Use an account UUID or email";
      }
      rows.add(new Row(n + 1, value, learner, error.isBlank(), error));
    }
    return rows;
  }

  public List<Map<String, Object>> apply(
      UUID actor,
      UUID org,
      List<String> entries,
      ProductType type,
      UUID product,
      Instant expiry,
      String cohort) {
    var plan = preview(actor, org, entries, type, product, expiry);
    if (cohort == null || cohort.length() > 120)
      throw new BadRequestException("IMPORT_COHORT", "Group label must be at most 120 characters");
    List<Map<String, Object>> results = new ArrayList<>();
    for (var row : plan) {
      Map<String, Object> result = new LinkedHashMap<>();
      result.put("row", row.row());
      result.put("identity", row.identity());
      result.put("learnerId", row.learnerId());
      if (!row.eligible()) {
        result.put("success", false);
        result.put("error", row.error());
      } else
        try {
          result.put(
              "grant",
              grants.grantImported(
                  actor, org, row.learnerId(), type, product, expiry, cohort.trim()));
          result.put("success", true);
        } catch (com.skillproof.backend.common.exception.BadRequestException
            | org.springframework.security.access.AccessDeniedException
            | com.skillproof.backend.common.exception.NotFoundException
            | com.skillproof.backend.common.exception.ConflictException ex) {
          result.put("success", false);
          result.put("error", ex.getMessage());
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
          result.put("success", false);
          result.put(
              "error", "Grant conflicts with an existing record; refresh and retry this row");
        }
      results.add(result);
    }
    return results;
  }
}
