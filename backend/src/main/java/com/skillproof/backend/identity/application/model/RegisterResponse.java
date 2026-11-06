package com.skillproof.backend.identity.application.model;

import com.skillproof.backend.identity.domain.AccountStatus;
import com.skillproof.backend.identity.domain.UserRole;
import java.time.Instant;
import java.util.UUID;

public record RegisterResponse(
    UUID id,
    String email,
    String displayName,
    UserRole role,
    AccountStatus status,
    Instant createdAt) {}
