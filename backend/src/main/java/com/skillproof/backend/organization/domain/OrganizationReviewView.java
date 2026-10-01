package com.skillproof.backend.organization.domain;

import java.time.Instant;
import java.util.UUID;

public record OrganizationReviewView(UUID reviewerUserId, Organization.Status decision,
        String reason, Instant reviewedAt) {

}
