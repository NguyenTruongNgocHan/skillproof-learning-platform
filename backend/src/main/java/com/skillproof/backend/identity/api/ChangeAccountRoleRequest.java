package com.skillproof.backend.identity.api;

import com.skillproof.backend.identity.domain.UserRole;

import jakarta.validation.constraints.NotNull;

public record ChangeAccountRoleRequest(
        @NotNull
        UserRole role
        ) {

}
