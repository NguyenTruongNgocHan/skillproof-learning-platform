package com.skillproof.backend.course.application;

import com.skillproof.backend.course.contract.CourseEnrollmentContinuity;
import com.skillproof.backend.course.infrastructure.CourseEnrollmentRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class CourseEnrollmentContinuityService implements CourseEnrollmentContinuity {
  private final CourseEnrollmentRepository enrollments;

  public CourseEnrollmentContinuityService(CourseEnrollmentRepository enrollments) {
    this.enrollments = enrollments;
  }

  public boolean enrolled(UUID actor, UUID version) {
    return enrollments.existsByLearnerIdAndVersionIdAndRetainedFreeAccessTrue(actor, version);
  }
}
