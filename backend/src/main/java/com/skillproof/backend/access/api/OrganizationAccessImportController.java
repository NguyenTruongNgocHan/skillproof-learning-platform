package com.skillproof.backend.access.api;

import com.skillproof.backend.access.application.*;
import com.skillproof.backend.commerce.domain.ProductType;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api/v1/access/organizations/{org}/imports")
public class OrganizationAccessImportController {
    private final OrganizationAccessImportService imports;

    public OrganizationAccessImportController(OrganizationAccessImportService imports) {
        this.imports = imports;
    }

    public record Input(
            @NotEmpty @Size(max = 1000) List<@NotBlank @Size(max = 320) String> entries,
            @NotNull ProductType type,
            @NotNull UUID productId,
            Instant expiresAt,
            @NotNull @Size(max = 120) String cohort) {}

    @PostMapping("/preview")
    public List<OrganizationAccessImportService.Row> preview(
            Authentication a, @PathVariable UUID org, @Valid @RequestBody Input i) {
        return imports.preview(
                (UUID) a.getPrincipal(), org, i.entries(), i.type(), i.productId(), i.expiresAt());
    }

    @PostMapping("/apply")
    public List<Map<String, Object>> apply(
            Authentication a, @PathVariable UUID org, @Valid @RequestBody Input i) {
        return imports.apply(
                (UUID) a.getPrincipal(),
                org,
                i.entries(),
                i.type(),
                i.productId(),
                i.expiresAt(),
                i.cohort());
    }
}
