package com.skillproof.backend.course.application;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillproof.backend.access.contract.AccessEntitlementQuery;
import com.skillproof.backend.commerce.domain.ProductType;
import com.skillproof.backend.common.exception.BadRequestException;
import com.skillproof.backend.common.exception.ConflictException;
import com.skillproof.backend.common.exception.NotFoundException;
import com.skillproof.backend.course.contract.CourseStructureQuery;
import com.skillproof.backend.course.infrastructure.CourseContextRepository;
import com.skillproof.backend.course.infrastructure.CourseEnrollmentRepository;
import com.skillproof.backend.course.infrastructure.CourseModuleJpaRepository;
import com.skillproof.backend.course.infrastructure.persistence.LessonRepository;

@Service
public class CourseStructureService implements CourseStructureQuery {

    @jakarta.persistence.PersistenceContext
    private jakarta.persistence.EntityManager entityManager;
    private final CourseContextRepository versions;
    private final CourseModuleJpaRepository modules;
    private final LessonRepository lessons;
    private final CourseEnrollmentRepository enrollments;
    private final CourseAccess access;
    private final AccessEntitlementQuery entitlements;

    public CourseStructureService(CourseContextRepository versions, CourseModuleJpaRepository modules, LessonRepository lessons,
            CourseEnrollmentRepository enrollments, CourseAccess access, AccessEntitlementQuery entitlements) {
        this.versions = versions;
        this.modules = modules;
        this.lessons = lessons;
        this.enrollments = enrollments;
        this.access = access;
        this.entitlements = entitlements;
    }

    @Override
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.MANDATORY)
    public void requireDraftAuthor(UUID actor, UUID versionId) {
        var version = versions.findForUpdate(versionId).orElseThrow(() -> new NotFoundException("VERSION_NOT_FOUND", "Version not found"));
        entityManager.refresh(version, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
        requireVersionAuthor(actor, versionId);
        if (!"DRAFT".equals(version.getStatus())) {
            throw new ConflictException("VERSION_IMMUTABLE", "Edit only draft content");
        }
        version.invalidateReview();
    }

    @Override
    public void requireVersionAuthor(UUID actor, UUID versionId) {
        access.manage(actor, versions.findCourseByVersionId(versionId).orElseThrow(() -> new NotFoundException("COURSE_NOT_FOUND", "Course not found")));
    }

    @Override
    public void validateOwner(UUID versionId, String scope, UUID ownerId) {
        boolean valid = switch (scope) {
            case "COURSE" ->
                versionId.equals(ownerId);
            case "MODULE" ->
                modules.findById(ownerId).map(m -> versionId.equals(m.getVersionId())).orElse(false);
            case "LESSON" ->
                lessons.findById(ownerId).flatMap(l -> modules.findById(l.getModuleId())).map(m -> versionId.equals(m.getVersionId())).orElse(false);
            default ->
                false;
        };
        if (!valid) {
            throw new BadRequestException("ACTIVITY_OWNER", "Exactly one owner in this course version is required");
        }
    }

    @Override
    public UUID enrollmentVersion(UUID learner, UUID enrollmentId) {
        return enrollments.findByIdAndLearnerId(enrollmentId, learner).orElseThrow(() -> new NotFoundException("ENROLLMENT_NOT_FOUND", "Enrollment not found")).getVersionId();
    }

    @Override
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.MANDATORY)
    public void lockEnrollment(UUID enrollmentId) {
        enrollments.findForCompletion(enrollmentId).orElseThrow();
    }

    @Override
    public void requireEnrollment(UUID actor, UUID enrollmentId, UUID versionId, boolean lock) {
        access.learner(actor);
        var enrollment = (lock ? enrollments.findForUpdate(enrollmentId, actor) : enrollments.findByIdAndLearnerId(enrollmentId, actor))
                .orElseThrow(() -> new NotFoundException("ENROLLMENT_NOT_FOUND", "Enrollment not found"));
        if (!versionId.equals(enrollment.getVersionId())) {
            throw new BadRequestException("ENROLLMENT_VERSION", "Activity is outside this enrollment version");
        }
        entitlements.require(actor, ProductType.COURSE, versionId);
    }
}
