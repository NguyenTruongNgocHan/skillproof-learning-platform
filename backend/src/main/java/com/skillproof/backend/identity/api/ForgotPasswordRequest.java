package com.skillproof.backend.identity.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ForgotPasswordRequest(
        @NotBlank
        @Email
        @Size(max = 320)
        String email
        ) {

    public com.skillproof.backend.identity.application.model.ForgotPasswordCommand toCommand() { return new com.skillproof.backend.identity.application.model.ForgotPasswordCommand(email); }
}
