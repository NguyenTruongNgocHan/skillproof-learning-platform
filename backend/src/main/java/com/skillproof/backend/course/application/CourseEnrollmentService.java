package com.skillproof.backend.course.application;

import java.time.Instant;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.skillproof.backend.common.exception.*;
import com.skillproof.backend.course.infrastructure.*;
import com.skillproof.backend.organization.contract.OrganizationPublicQuery;
import com.skillproof.backend.access.contract.AccessEntitlementQuery;
import com.skillproof.backend.commerce.domain.ProductType;

@Service
public class CourseEnrollmentService {

    private final CourseEnrollmentRepository enrollments;
    private final CourseJpaRepository courses;
    private final CourseContextRepository versions;
    private final CourseAccess access;
    private final AccessEntitlementQuery entitlements;
    private final OrganizationPublicQuery organizations;

    public CourseEnrollmentService(CourseEnrollmentRepository enrollments, CourseJpaRepository courses, CourseContextRepository versions,
            CourseAccess access, AccessEntitlementQuery entitlements, OrganizationPublicQuery organizations) {
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
        var snapshot = courses.findById(courseId).orElseThrow(() -> new NotFoundException("COURSE_NOT_FOUND", "Course not found"));
        if (snapshot.getOrganizationId() != null) {
            organizations.requireApprovedForUpdate(snapshot.getOrganizationId());
        }
        courses.findForUpdate(courseId).orElseThrow();
        var existing = enrollments.findByLearnerIdAndCourseId(learner, courseId);
        if (existing.isPresent()) {
            entitlements.require(learner, ProductType.COURSE, existing.get().getVersionId());
            return view(existing.get());
        }
        var version = versions.findFirstByCourseIdAndStatusOrderByVersionNoDesc(courseId, "PUBLISHED")
                .orElseThrow(() -> new ConflictException("COURSE_NOT_PUBLISHED", "Published version required"));
        entitlements.require(learner, ProductType.COURSE, version.getId());
        return view(enrollments.save(new EnrollmentEntity(UUID.randomUUID(), learner, courseId, version.getId(), "ACTIVE", Instant.now())));
    }

    @Transactional
    public Map<String, Object> enrollVersion(UUID learner, UUID versionId) {
        access.learner(learner);
        var version = versions.findById(versionId).orElseThrow(() -> new NotFoundException("VERSION_NOT_FOUND", "Version not found"));
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
                throw new ConflictException("ENROLLMENT_PINNED", "Existing enrollment uses another version");
            }
            return view(existing.get());
        }
        return view(enrollments.save(new EnrollmentEntity(UUID.randomUUID(), learner, course.getId(), versionId, "ACTIVE", Instant.now())));
    }

    private Map<String, Object> view(EnrollmentEntity e) {
        return Map.of("id", e.getId(), "learner_id", e.getLearnerId(), "course_id", e.getCourseId(), "version_id", e.getVersionId(), "status", e.getStatus());
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> mine(UUID learner) {
        access.learner(learner);
        return enrollments.findByLearnerIdOrderByEnrolledAtDesc(learner).stream().map(this::view).toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> enrollment(UUID learner, UUID id) {
        access.learner(learner);
        var e = enrollments.findByIdAndLearnerId(id, learner).orElseThrow(() -> new NotFoundException("ENROLLMENT_NOT_FOUND", "Enrollment not found"));
        entitlements.require(learner, ProductType.COURSE, e.getVersionId());
        return view(e);
    }
}
