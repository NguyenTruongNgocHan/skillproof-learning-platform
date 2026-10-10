package com.skillproof.backend.identity.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(@NotBlank
        @Email
        String email, @NotBlank
        String password) {

    public com.skillproof.backend.identity.application.model.LoginCommand toCommand() {
        return new com.skillproof.backend.identity.application.model.LoginCommand(email, password);
    }
}
