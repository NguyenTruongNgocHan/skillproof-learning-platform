package com.skillproof.backend.organization.api;

import com.skillproof.backend.organization.application.OrganizationPlatformService;
import com.skillproof.backend.organization.domain.Organization;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/organization-status")
public class AdminOrganizationStatusController {
    private final OrganizationPlatformService service;

    public AdminOrganizationStatusController(OrganizationPlatformService service) {
        this.service = service;
    }

    public record StatusInput(boolean suspended, @NotBlank @Size(max = 1000) String reason) {}

    @GetMapping("/{id}")
    public Organization get(Authentication a, @PathVariable UUID id) {
        return service.get((UUID) a.getPrincipal(), id);
    }

    @PutMapping("/{id}")
    public Organization status(
            Authentication a, @PathVariable UUID id, @Valid @RequestBody StatusInput i) {
        return service.status((UUID) a.getPrincipal(), id, i.suspended(), i.reason());
    }
}
