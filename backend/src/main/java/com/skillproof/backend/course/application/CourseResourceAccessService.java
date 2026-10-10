package com.skillproof.backend.course.application;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillproof.backend.access.contract.AccessEntitlementQuery;
import com.skillproof.backend.commerce.domain.ProductType;
import com.skillproof.backend.common.exception.NotFoundException;
import com.skillproof.backend.course.contract.CourseResourceAccessQuery;
import com.skillproof.backend.course.infrastructure.CourseContextRepository;
import com.skillproof.backend.course.infrastructure.CourseEnrollmentRepository;
import com.skillproof.backend.course.infrastructure.CourseModuleJpaRepository;
import com.skillproof.backend.course.infrastructure.CourseResourceEntity;
import com.skillproof.backend.course.infrastructure.CourseResourceJpaRepository;

@Service
public class CourseResourceAccessService implements CourseResourceAccessQuery {

    private final CourseResourceJpaRepository resources;
    private final CourseModuleJpaRepository modules;
    private final CourseContextRepository versions;
    private final CourseStructureService structure;
    private final CourseAccess access;
    private final AccessEntitlementQuery entitlements;
    private final CourseEnrollmentRepository enrollments;

    public CourseResourceAccessService(CourseResourceJpaRepository resources, CourseModuleJpaRepository modules, CourseContextRepository versions,
            CourseStructureService structure, CourseAccess access, AccessEntitlementQuery entitlements, CourseEnrollmentRepository enrollments) {
        this.resources = resources;
        this.modules = modules;
        this.versions = versions;
        this.structure = structure;
        this.access = access;
        this.entitlements = entitlements;
        this.enrollments = enrollments;
    }

    private CourseResourceEntity resource(UUID id) {
        return resources.findById(id).orElseThrow(() -> new NotFoundException("RESOURCE_NOT_FOUND", "Resource not found"));
    }

    private UUID version(CourseResourceEntity r) {
        return modules.findById(r.getModuleId()).orElseThrow().getVersionId();
    }

    @Override
    @Transactional
    public void requireDraftManage(UUID actor, UUID resourceId) {
        structure.requireDraftAuthor(actor, version(resource(resourceId)));
    }

    @Override
    public void requireRead(UUID actor, UUID resourceId) {
        if (access.canReview(actor)) {
            return;
        }
        var r = resource(resourceId);
        UUID versionId = version(r);
        var course = versions.findCourseByVersionId(versionId).orElseThrow();
        try {
            access.manage(actor, course);
            return;
        } catch (org.springframework.security.access.AccessDeniedException denied) {
            /* Continue through learner access. */ }
        access.learner(actor);
        entitlements.require(actor, ProductType.COURSE, versionId);
        if (!enrollments.existsByLearnerIdAndVersionId(actor, versionId)) {
            throw new org.springframework.security.access.AccessDeniedException("Enrollment required");
        }
    }

    @Override
    public String resourceKind(UUID resourceId) {
        return resource(resourceId).getKind();
    }
}
