package com.skillproof.backend.certification.api;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record EvaluateEligibilityRequest(@NotNull
        UUID enrollmentId) {

}
