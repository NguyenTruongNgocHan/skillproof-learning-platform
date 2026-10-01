package com.skillproof.backend.learning.contract;

import java.util.UUID;

/**
 * Public Learning contract for Media authorization and resource metadata.
 */
public interface LearningResourceAccessQuery {

    void requireDraftManage(UUID actorId, UUID resourceId);

    void requireRead(UUID actorId, UUID resourceId);

    String resourceKind(UUID resourceId);
}
