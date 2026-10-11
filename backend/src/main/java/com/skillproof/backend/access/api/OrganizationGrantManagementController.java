package com.skillproof.backend.access.api;

import com.skillproof.backend.access.application.AccessGrantService;
import com.skillproof.backend.access.infrastructure.persistence.AccessGrantEntity;
import com.skillproof.backend.commerce.domain.ProductType;

import jakarta.validation.*;
import jakarta.validation.constraints.*;

import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.util.*;

@RestController
@RequestMapping("/api/v1/access/organizations/{org}")
public class OrganizationGrantManagementController {
    private final AccessGrantService service;

    public OrganizationGrantManagementController(AccessGrantService service) {
        this.service = service;
    }

    public record BulkInput(
            @NotEmpty @Size(max = 100) List<UUID> learnerIds,
            @NotNull ProductType type,
            @NotNull UUID productId,
            Instant expiresAt) {}

    @GetMapping("/grants")
    public Page<AccessGrantEntity> list(
            Authentication a,
            @PathVariable UUID org,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "") String query,
            @RequestParam(defaultValue = "") String status) {
        return service.search((UUID) a.getPrincipal(), org, query, status, page);
    }

    @PostMapping("/grants/bulk")
    public List<Map<String, Object>> bulk(
            Authentication a, @PathVariable UUID org, @Valid @RequestBody BulkInput i) {
        UUID actor = (UUID) a.getPrincipal();
        service.organization(actor, org, 0);
        return i.learnerIds().stream()
                .distinct()
                .map(
                        id -> {
                            Map<String, Object> r = new LinkedHashMap<>();
                            r.put("learnerId", id);
                            try {
                                r.put(
                                        "grant",
                                        service.grantOrganization(
                                                actor,
                                                org,
                                                id,
                                                i.type(),
                                                i.productId(),
                                                i.expiresAt()));
                                r.put("success", true);
                            } catch (com.skillproof.backend.common.exception.BadRequestException
                                    | org.springframework.security.access.AccessDeniedException
                                            ex) {
                                r.put("success", false);
                                r.put("error", ex.getMessage());
                            }
                            return r;
                        })
                .toList();
    }
}
