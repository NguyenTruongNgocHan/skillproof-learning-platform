package com.skillproof.backend.course.contract;

import java.util.Optional;
import java.util.UUID;

public interface CoursePlanQuery {

    record CourseItem(UUID courseId, UUID versionId, String title, boolean completed) {

    }

    CourseItem item(UUID learner, UUID versionId);

    Optional<UUID> enrolledVersion(UUID learner, UUID courseId);
}
