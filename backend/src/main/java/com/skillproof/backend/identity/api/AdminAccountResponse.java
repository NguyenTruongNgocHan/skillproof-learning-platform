package com.skillproof.backend.identity.api;

import com.skillproof.backend.identity.domain.AccountStatus;
import com.skillproof.backend.identity.domain.UserAccount;
import com.skillproof.backend.identity.domain.UserRole;

import java.time.Instant;
import java.util.UUID;

public record AdminAccountResponse(
        UUID id,
        String email,
        String displayName,
        UserRole role,
        AccountStatus status,
        Instant lastLoginAt,
        Instant createdAt
) {
    public static AdminAccountResponse from(UserAccount account) {
        return new AdminAccountResponse(
                account.getId(),
                account.getEmail(),
                account.getDisplayName(),
                account.getRole(),
                account.getStatus(),
                account.getLastLoginAt(),
                account.getCreatedAt()
        );
    }
}
