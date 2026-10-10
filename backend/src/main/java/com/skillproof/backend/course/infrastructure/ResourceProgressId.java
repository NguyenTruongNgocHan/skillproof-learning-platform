package com.skillproof.backend.course.infrastructure;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class ResourceProgressId implements Serializable {

    private UUID enrollmentId;
    private UUID resourceId;

    public ResourceProgressId() {
    }

    public ResourceProgressId(UUID enrollmentId, UUID resourceId) {
        this.enrollmentId = enrollmentId;
        this.resourceId = resourceId;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ResourceProgressId that)) {
            return false;
        }
        return Objects.equals(enrollmentId, that.enrollmentId)
                && Objects.equals(resourceId, that.resourceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enrollmentId, resourceId);
    }
}
