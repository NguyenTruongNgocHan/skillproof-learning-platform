package com.skillproof.backend.course.application;

import java.util.UUID;
import java.util.Locale;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;

import org.springframework.stereotype.Service;

import com.skillproof.backend.identity.contract.IdentityAccessQuery;
import com.skillproof.backend.course.contract.EnrollmentLookupQuery;
import com.skillproof.backend.course.infrastructure.EnrollmentEntity;
import com.skillproof.backend.course.infrastructure.CourseContextRepository;
import com.skillproof.backend.course.infrastructure.CourseEnrollmentRepository;
import com.skillproof.backend.course.infrastructure.CourseJpaRepository;

@Service
public class EnrollmentLookupQueryService implements EnrollmentLookupQuery {

    private final CourseContextRepository versions;
    private final CourseEnrollmentRepository enrollments;
    private final CourseJpaRepository paths;
    private final IdentityAccessQuery identities;

    public EnrollmentLookupQueryService(
            CourseContextRepository versions,
            CourseEnrollmentRepository enrollments,
            CourseJpaRepository paths,
            IdentityAccessQuery identities) {
        this.versions = versions;
        this.enrollments = enrollments;
        this.paths = paths;
        this.identities = identities;
    }

    @Override
    public Page<EnrollmentSummary> search(
            UUID organizationId, UUID pathVersionId, String query, int page, int size) {
        var context = versions.findVersionContext(pathVersionId)
                .filter(value -> organizationId.equals(value.getOrganizationId()))
                .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("Course path version is not owned by organization"));
        var version = versions.findById(pathVersionId)
                .orElseThrow(() -> new com.skillproof.backend.common.exception.NotFoundException("VERSION_NOT_FOUND", "Course path version not found"));
        var path = paths.findById(version.getCourseId()).orElse(null);
        String normalized = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        int pageSize = Math.min(Math.max(size, 1), 100);
        var matches = normalized.isEmpty() ? java.util.List.<UUID>of() : identities.findAccountIdsByEmailFragment(normalized);
        // Avoid empty IN provider differences; impossible account sentinel for an empty match set.
        var learnerIds = matches.isEmpty() ? java.util.List.of(new UUID(0, 0)) : matches;
        return enrollments.searchByVersionId(pathVersionId, normalized, learnerIds, PageRequest.of(Math.max(0, page), pageSize)).map(
                enrollment -> toSummary(enrollment, context, version.getVersionNo(),
                        path == null ? "" : path.getTitle()));
    }

    private EnrollmentSummary toSummary(
            EnrollmentEntity enrollment,
            CourseContextRepository.VersionProjection context,
            int versionNo,
            String pathTitle) {
        String email = identities.find(enrollment.getLearnerId())
                .map(IdentityAccessQuery.Account::email)
                .orElse("Unknown learner");
        return new EnrollmentSummary(
                enrollment.getId(),
                enrollment.getLearnerId(),
                email,
                context.getId(),
                versionNo,
                pathTitle,
                enrollment.getStatus(),
                enrollment.getEnrolledAt());
    }
}
