package com.skillproof.backend.identity.api;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.skillproof.backend.identity.application.LearnerPreferencesService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/v1/me/learning-preferences")
@SecurityRequirement(name = "bearerAuth")
public class LearnerPreferencesController {

    private final LearnerPreferencesService service;

    public LearnerPreferencesController(LearnerPreferencesService service) {
        this.service = service;
    }

    public record DiscoveryPreferences(
            Boolean personalizationEnabled,
            Boolean explorationMode,

            @Size(max = 1000)
            String goalText,

            @Size(max = 30)
            String experienceLevel,

            @Size(max = 20)
            List<@Size(min = 1, max = 100) String> interests) {
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