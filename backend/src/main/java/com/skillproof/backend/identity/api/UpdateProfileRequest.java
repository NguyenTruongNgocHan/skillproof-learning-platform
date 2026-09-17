package com.skillproof.backend.identity.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @NotBlank @Size(min = 2, max = 100) String displayName,
        @Size(max = 160) String headline,
        @Size(max = 1000) String bio,
        @Size(max = 500) String avatarUrl,
        @NotBlank @Size(max = 16) String locale,
        @NotBlank @Size(max = 64) String timezone) {
}
