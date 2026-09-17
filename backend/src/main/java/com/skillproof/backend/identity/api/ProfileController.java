package com.skillproof.backend.identity.api;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.skillproof.backend.identity.application.ProfileService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/me")
@SecurityRequirement(name = "bearerAuth")
public class ProfileController {

    private final ProfileService profiles;

    public ProfileController(ProfileService profiles) {
        this.profiles = profiles;
    }

    @GetMapping
    public ProfileResponse get(Authentication authentication) {
        return profiles.get((UUID) authentication.getPrincipal());
    }

    @PatchMapping("/profile")
    public ProfileResponse update(Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request) {
        return profiles.update((UUID) authentication.getPrincipal(), request);
    }
}
