package com.skillproof.backend.identity.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VerifyEmailRequest(
        @NotBlank
        @Size(min = 32, max = 512)
        String token
        ) {

    public com.skillproof.backend.identity.application.model.VerifyEmailCommand toCommand() {
        return new com.skillproof.backend.identity.application.model.VerifyEmailCommand(token);
    }
}
