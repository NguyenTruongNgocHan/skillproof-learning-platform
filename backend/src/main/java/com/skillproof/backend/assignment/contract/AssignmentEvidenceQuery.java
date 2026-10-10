package com.skillproof.backend.assignment.contract;

import java.util.UUID;

public interface AssignmentEvidenceQuery {

    record Evidence(int required, int passed) {

    }

    Evidence evidence(UUID versionId, UUID enrollmentId);
}
