package com.skillproof.backend.access.api;

import java.time.Instant;
import java.util.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.skillproof.backend.access.application.AccessGrantService;
import com.skillproof.backend.access.infrastructure.persistence.AccessGrantEntity;
import com.skillproof.backend.commerce.domain.ProductType;

@RestController
@RequestMapping("/api/v1/access")
public class AccessGrantController {

    private final AccessGrantService service;

    public AccessGrantController(AccessGrantService service) {
        this.service = service;
    }

    public record GrantInput(@NotNull UUID learnerId, @NotNull ProductType type, @NotNull UUID productId, Instant expiresAt) {

    }

    @PostMapping("/organizations/{organization}/grants")
    public AccessGrantEntity grant(Authentication auth, @PathVariable UUID organization, @Valid @RequestBody GrantInput input) {
        return service.grantOrganization((UUID) auth.getPrincipal(), organization, input.learnerId(), input.type(), input.productId(), input.expiresAt());
    }

    @DeleteMapping("/organizations/{organization}/grants/{id}")
    @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
    public void revoke(Authentication auth, @PathVariable UUID organization, @PathVariable UUID id) {
        service.revoke((UUID) auth.getPrincipal(), organization, id);
    }

    @GetMapping("/me/grants")
    public List<AccessGrantEntity> mine(Authentication auth) {
        return service.mine((UUID) auth.getPrincipal());
    }
}
