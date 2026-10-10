package com.skillproof.backend.assignment.contract;

import java.util.UUID;

public interface AssignmentMediaAccess {

    void requireWrite(UUID actor, UUID submissionId);

    void requireRead(UUID actor, UUID submissionId);
}
