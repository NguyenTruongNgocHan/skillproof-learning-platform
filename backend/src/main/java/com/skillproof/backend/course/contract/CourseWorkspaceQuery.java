package com.skillproof.backend.course.contract;

import java.util.*;

public interface CourseWorkspaceQuery {
    record Context(
            UUID courseId,
            UUID versionId,
            UUID organizationId,
            UUID author,
            String status,
            String title,
            long price,
            long certificationPrice,
            String accessMode) {}

    void lockCourse(UUID course);

    Context context(UUID version);

    List<UUID> versions(UUID organization);

    boolean hasExperience(UUID actor, UUID version);

    UUID versionForCourse(UUID course);

    void requireAuthor(UUID actor, UUID version);

    void requireGrader(UUID actor, UUID version);

    void requireCourseAuthor(UUID actor, UUID course);
}
