package com.skillproof.backend.certification.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreateCertificationProgramRequest(
    @NotNull UUID courseVersionId, @NotBlank @Size(max = 180) String name) {}
