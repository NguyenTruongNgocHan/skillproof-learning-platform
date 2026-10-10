package com.skillproof.backend.assignment.contract;

import java.util.Map;
import java.util.UUID;

public interface AssignmentVersionBridge {

    void cloneVersion(UUID source, UUID target, Map<UUID, UUID> modules, Map<UUID, UUID> lessons);

    boolean hasOwner(String scope, UUID ownerId);
}
