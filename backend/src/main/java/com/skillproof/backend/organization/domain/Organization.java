package com.skillproof.backend.organization.domain;

import java.time.Instant;
import java.util.UUID;

public record Organization(UUID id, UUID ownerUserId, String legalName, String displayName,
        String website, String industry, String country, String registrationNumber,
        String contactName, String contactEmail, String contactPhone, Status status,
        String reviewReason, Instant createdAt, Instant updatedAt) {

    public enum Status {
        PENDING, APPROVED, REJECTED
    }
}
