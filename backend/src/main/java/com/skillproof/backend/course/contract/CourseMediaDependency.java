package com.skillproof.backend.course.contract;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

/**
 * Narrow collaboration contract for Media-owned asset lifecycle operations.
 */
public interface CourseMediaDependency {

    void deleteForResources(Collection<UUID> resourceIds);

    void cloneResourceAssets(Map<UUID, UUID> resourceIdMapping);

    boolean hasAssetsForAll(Collection<UUID> resourceIds);
}
