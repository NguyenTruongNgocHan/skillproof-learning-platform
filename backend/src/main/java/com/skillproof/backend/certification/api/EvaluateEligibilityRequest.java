package com.skillproof.backend.certification.api;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record EvaluateEligibilityRequest(@NotNull UUID enrollmentId) {}
