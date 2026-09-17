package com.skillproof.backend.identity.api;

import com.skillproof.backend.identity.domain.AccountStatus;

import jakarta.validation.constraints.NotNull;

public record ChangeAccountStatusRequest(
        @NotNull
        AccountStatus status
        ) {

}
