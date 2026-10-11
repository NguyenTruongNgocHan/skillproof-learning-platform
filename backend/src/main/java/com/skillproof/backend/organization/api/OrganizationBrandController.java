package com.skillproof.backend.organization.api;

import com.skillproof.backend.organization.application.OrganizationBrandService;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1")
public class OrganizationBrandController {
    private final OrganizationBrandService service;

    public OrganizationBrandController(OrganizationBrandService service) {
        this.service = service;
    }

    public record Input(UUID logoMediaId, UUID coverMediaId) {}

    @GetMapping("/organizations/{org}/branding")
    public Map<String, Object> get(Authentication a, @PathVariable UUID org) {
        return service.read((UUID) a.getPrincipal(), org);
    }

    @PutMapping("/organizations/{org}/branding")
    public Map<String, Object> update(
            Authentication a, @PathVariable UUID org, @RequestBody Input i) {
        return service.update((UUID) a.getPrincipal(), org, i.logoMediaId(), i.coverMediaId());
    }

    @GetMapping("/public/organizations/{org}")
    public Map<String, Object> publicProfile(@PathVariable UUID org) {
        return service.publicProfile(org);
    }

    @GetMapping("/public/organizations/{org}/branding/{kind}")
    public ResponseEntity<InputStreamResource> image(
            @PathVariable UUID org, @PathVariable String kind) {
        var file = service.image(org, kind);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.mime()))
                .contentLength(file.size())
                .header("X-Content-Type-Options", "nosniff")
                .header("Cache-Control", "public, max-age=300")
                .body(new InputStreamResource(file.stream()));
    }
}
