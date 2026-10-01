package com.skillproof.backend.certification.api;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateCertificationProgramRequest(
        @NotNull
        UUID learningPathVersionId,
        @NotBlank
        @Size(max = 180)
        String name) {

}
