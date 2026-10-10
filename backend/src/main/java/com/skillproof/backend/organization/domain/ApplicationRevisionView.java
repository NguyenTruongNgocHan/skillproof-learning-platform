package com.skillproof.backend.organization.domain;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ApplicationRevisionView(
        UUID id, UUID organizationId, int revisionNo, String legalName, String displayName,
        String website, String industry, String country, String registrationNumber,
        String contactName, String contactEmail, String contactPhone, String status,
        UUID reviewerUserId, String reviewReason, List<UUID> documentMediaIds,
        Instant submittedAt, Instant reviewedAt) {

}
