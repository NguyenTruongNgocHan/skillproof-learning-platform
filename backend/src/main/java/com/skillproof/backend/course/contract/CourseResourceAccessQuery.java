package com.skillproof.backend.course.contract;

import java.util.UUID;

/**
 * Public Course contract for Media authorization and resource metadata.
 */
public interface CourseResourceAccessQuery {

    void requireDraftManage(UUID actorId, UUID resourceId);

    void requireRead(UUID actorId, UUID resourceId);

    String resourceKind(UUID resourceId);
}
