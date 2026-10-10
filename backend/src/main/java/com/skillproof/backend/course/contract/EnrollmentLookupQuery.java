package com.skillproof.backend.course.contract;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.domain.Page;

public interface EnrollmentLookupQuery {

    record EnrollmentSummary(
            UUID id,
            UUID learnerId,
            String learnerEmail,
            UUID pathVersionId,
            int versionNo,
            String pathTitle,
            String status,
            Instant enrolledAt) {

    }

    Page<EnrollmentSummary> search(
            UUID organizationId,
            UUID pathVersionId,
            String query,
            int page,
            int size);
}
