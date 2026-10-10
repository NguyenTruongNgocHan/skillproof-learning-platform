package com.skillproof.backend.identity.application.model;

import java.util.List;

import jakarta.validation.constraints.Size;

public record DiscoveryPreferences(Boolean personalizationEnabled, Boolean explorationMode,
        @Size(max = 1000)
        String goalText, @Size(max = 30)
        String experienceLevel,
        @Size(max = 20)
        List<@Size(min = 1, max = 100) String> interests) {

}
