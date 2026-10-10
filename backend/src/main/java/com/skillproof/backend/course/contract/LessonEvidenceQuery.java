package com.skillproof.backend.course.contract;

import java.util.UUID;

public interface LessonEvidenceQuery {

    record Evidence(int required, int completed) {

    }

    Evidence evidence(UUID versionId, UUID enrollmentId);
}
