package com.skillproof.backend.course.application;

import com.skillproof.backend.access.contract.AccessEntitlementQuery;
import com.skillproof.backend.commerce.domain.ProductType;
import com.skillproof.backend.common.exception.*;
import com.skillproof.backend.course.infrastructure.*;
import com.skillproof.backend.organization.contract.OrganizationPublicQuery;
import java.time.Instant;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CourseEnrollmentService {
  @org.springframework.beans.factory.annotation.Autowired
  private com.skillproof.backend.analytics.contract.LearningSignalJournal signals;

  @org.springframework.beans.factory.annotation.Autowired
  private com.skillproof.backend.pricing.contract.CourseSaleQuery sales;

  @org.springframework.beans.factory.annotation.Autowired
  private com.skillproof.backend.analytics.contract.MarketingJournal marketing;

  private final CourseEnrollmentRepository enrollments;
  private final CourseJpaRepository courses;
  private final CourseContextRepository versions;
  private final CourseAccess access;
  private final AccessEntitlementQuery entitlements;
  private final OrganizationPublicQuery organizations;

  public CourseEnrollmentService(
      CourseEnrollmentRepository enrollments,
      CourseJpaRepository courses,
      CourseContextRepository versions,
      CourseAccess access,
      AccessEntitlementQuery entitlements,
      OrganizationPublicQuery organizations) {
    this.enrollments = enrollments;
    this.courses = courses;
    this.versions = versions;
    this.access = access;
    this.entitlements = entitlements;
    this.organizations = organizations;
  }

  @Transactional
  public Map<String, Object> enroll(UUID learner, UUID courseId) {
    access.learner(learner);
    var snapshot =
        courses
            .findById(courseId)
            .orElseThrow(() -> new NotFoundException("COURSE_NOT_FOUND", "Course not found"));
    if (snapshot.getOrganizationId() != null) {
      organizations.requireApprovedForUpdate(snapshot.getOrganizationId());
    }
    courses.findForUpdate(courseId).orElseThrow();
    var existing = enrollments.findByLearnerIdAndCourseId(learner, courseId);
    if (existing.isPresent()) {
      entitlements.require(learner, ProductType.COURSE, existing.get().getVersionId());
      return view(existing.get());
    }
    var version =
        versions
            .findFirstByCourseIdAndStatusOrderByVersionNoDesc(courseId, "PUBLISHED")
            .orElseThrow(
                () -> new ConflictException("COURSE_NOT_PUBLISHED", "Published version required"));
    if (!"ACTIVE"
        .equals(
            sales
                .sale(
                    version.getId(),
                    version.getAccessMode(),
                    version.getPriceVnd(),
                    version.getCertificationPriceVnd())
                .state()))
      throw new ConflictException("COURSE_CLOSED", "Course is not accepting new enrollments");
    entitlements.require(learner, ProductType.COURSE, version.getId());
    var row =
        new EnrollmentEntity(
            UUID.randomUUID(), learner, courseId, version.getId(), "ACTIVE", Instant.now());
    retainFree(row, version);
    var enrolled = enrollments.save(row);
    marketing.conversion(learner, version.getId(), "ENROLLMENT", enrolled.getId());
    signals.record(learner, version.getId(), "ENROLLED", enrolled.getId(), Map.of());
    return view(enrolled);
  }

  @Transactional
  public Map<String, Object> enrollVersion(UUID learner, UUID versionId) {
    access.learner(learner);
    var version =
        versions
            .findById(versionId)
            .orElseThrow(() -> new NotFoundException("VERSION_NOT_FOUND", "Version not found"));
    var course = courses.findById(version.getCourseId()).orElseThrow();
    if (course.getOrganizationId() != null) {
      organizations.requireApprovedForUpdate(course.getOrganizationId());
    }
    courses.findForUpdate(course.getId()).orElseThrow();
    if ("DRAFT".equals(version.getStatus())) {
      throw new ConflictException("VERSION_UNPUBLISHED", "Published course required");
    }
    entitlements.require(learner, ProductType.COURSE, versionId);
    var existing = enrollments.findByLearnerIdAndCourseId(learner, course.getId());
    if (existing.isPresent()) {
      if (!versionId.equals(existing.get().getVersionId())) {
        throw new ConflictException(
            "ENROLLMENT_PINNED", "Existing enrollment uses another version");
      }
      return view(existing.get());
    }
    if (!"ACTIVE"
        .equals(
            sales
                .sale(
                    version.getId(),
                    version.getAccessMode(),
                    version.getPriceVnd(),
                    version.getCertificationPriceVnd())
                .state()))
      throw new ConflictException("COURSE_CLOSED", "Course is not accepting new enrollments");
    var row =
        new EnrollmentEntity(
            UUID.randomUUID(), learner, course.getId(), versionId, "ACTIVE", Instant.now());
    retainFree(row, version);
    var enrolled = enrollments.save(row);
    marketing.conversion(learner, versionId, "ENROLLMENT", enrolled.getId());
    signals.record(learner, versionId, "ENROLLED", enrolled.getId(), Map.of());
    return view(enrolled);
  }

  private void retainFree(EnrollmentEntity row, CourseVersionEntity version) {
    var sale =
        sales.sale(
            version.getId(),
            version.getAccessMode(),
            version.getPriceVnd(),
            version.getCertificationPriceVnd());
    row.retainFreeAccess("PUBLIC".equals(sale.accessMode()) && sale.learningPrice() == 0);
  }

  private Map<String, Object> view(EnrollmentEntity e) {
    var v = versions.findById(e.getVersionId()).orElseThrow();
    var row = new java.util.LinkedHashMap<String, Object>();
    row.put("id", e.getId());
    row.put("learner_id", e.getLearnerId());
    row.put("course_id", e.getCourseId());
    row.put("version_id", e.getVersionId());
    row.put("version_no", v.getVersionNo());
    row.put("title", v.getPublishedTitle());
    row.put("summary", v.getPublishedSummary());
    row.put("status", e.getStatus());
    row.put("enrolled_at", e.getEnrolledAt());
    row.put("completed_at", e.getCompletedAt());
    return row;
  }

  @Transactional(readOnly = true)
  public List<Map<String, Object>> mine(UUID learner) {
    access.learner(learner);
    return enrollments.findByLearnerIdOrderByEnrolledAtDesc(learner).stream()
        .map(this::view)
        .toList();
  }

  @Transactional(readOnly = true)
  public Map<String, Object> enrollment(UUID learner, UUID id) {
    access.learner(learner);
    var e =
        enrollments
            .findByIdAndLearnerId(id, learner)
            .orElseThrow(
                () -> new NotFoundException("ENROLLMENT_NOT_FOUND", "Enrollment not found"));
    entitlements.require(learner, ProductType.COURSE, e.getVersionId());
    return view(e);
  }
}
