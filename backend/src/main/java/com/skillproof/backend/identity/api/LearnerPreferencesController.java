package com.skillproof.backend.identity.api;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.skillproof.backend.identity.application.LearnerPreferencesService;
import com.skillproof.backend.identity.application.model.DiscoveryPreferences;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/me/learning-preferences")
@SecurityRequirement(name = "bearerAuth")
public class LearnerPreferencesController {

    private final LearnerPreferencesService service;

    public LearnerPreferencesController(LearnerPreferencesService service) {
        this.service = service;
    }

    @GetMapping
    public LearnerPreferencesService.State get(Authentication auth) {
        return service.get((UUID) auth.getPrincipal());
    }

    @PutMapping
    public LearnerPreferencesService.State save(
            Authentication auth,
            @Valid @RequestBody DiscoveryPreferences input) {

        return service.save(
                (UUID) auth.getPrincipal(),
                input,
                LearnerPreferencesService.InterestSource.USER_SELECTED);
    }

    @DeleteMapping
    public void delete(Authentication auth) {
        service.delete((UUID) auth.getPrincipal());
    }
}
