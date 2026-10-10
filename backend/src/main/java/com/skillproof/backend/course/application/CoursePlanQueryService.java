package com.skillproof.backend.course.application;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.skillproof.backend.common.exception.BadRequestException;
import com.skillproof.backend.common.exception.NotFoundException;
import com.skillproof.backend.course.contract.CoursePlanQuery;
import com.skillproof.backend.course.infrastructure.CourseContextRepository;
import com.skillproof.backend.course.infrastructure.CourseEnrollmentRepository;
import com.skillproof.backend.course.infrastructure.EnrollmentEntity;

@Service
public class CoursePlanQueryService implements CoursePlanQuery {

    private final CourseContextRepository versions;
    private final CourseEnrollmentRepository enrollments;

    public CoursePlanQueryService(CourseContextRepository versions, CourseEnrollmentRepository enrollments) {
        this.versions = versions;
        this.enrollments = enrollments;
    }

    @Override
    public CourseItem item(UUID learner, UUID versionId) {
        var v = versions.findById(versionId).orElseThrow(() -> new NotFoundException("VERSION_NOT_FOUND", "Course version not found"));
        if ("DRAFT".equals(v.getStatus())) {
            throw new BadRequestException("DRAFT_COURSE", "A path may contain only published course versions");
        }
        boolean completed = enrollments.findByLearnerIdAndCourseId(learner, v.getCourseId()).filter(e -> versionId.equals(e.getVersionId()) && "COMPLETED".equals(e.getStatus())).isPresent();
        return new CourseItem(v.getCourseId(), v.getId(), v.getPublishedTitle(), completed);
    }

    @Override
    public Optional<UUID> enrolledVersion(UUID learner, UUID courseId) {
        return enrollments.findByLearnerIdAndCourseId(learner, courseId).map(EnrollmentEntity::getVersionId);
    }
}
