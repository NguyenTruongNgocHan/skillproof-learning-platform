package com.skillproof.backend.course.contract;

import java.util.UUID;

public interface CourseEnrollmentContinuity {
    boolean enrolled(UUID actor, UUID version);
}
