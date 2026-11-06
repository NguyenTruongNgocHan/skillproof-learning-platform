package com.skillproof.backend.course.contract;

import java.util.UUID;

public interface CourseStructureQuery {

  void requireDraftAuthor(UUID actor, UUID versionId);

  void requireVersionAuthor(UUID actor, UUID versionId);

  void requireVersionReader(UUID actor, UUID versionId);

  void validateOwner(UUID versionId, String scope, UUID ownerId);

  UUID enrollmentVersion(UUID learner, UUID enrollmentId);

  void lockEnrollment(UUID enrollmentId);

  void requireEnrollment(UUID actor, UUID enrollmentId, UUID versionId, boolean lock);

  /** Ownership-only access to already recorded results, independent of current content access. */
  void requireHistoricalEnrollment(UUID actor, UUID enrollmentId, UUID versionId);
}
