package com.skillproof.backend.learning.application;

import java.util.UUID;
import java.util.Locale;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;

import org.springframework.stereotype.Service;

import com.skillproof.backend.identity.contract.IdentityAccessQuery;
import com.skillproof.backend.learning.contract.EnrollmentLookupQuery;
import com.skillproof.backend.learning.infrastructure.EnrollmentEntity;
import com.skillproof.backend.learning.infrastructure.LearningContextRepository;
import com.skillproof.backend.learning.infrastructure.LearningEnrollmentRepository;
import com.skillproof.backend.learning.infrastructure.LearningPathJpaRepository;

@Service
public class EnrollmentLookupQueryService implements EnrollmentLookupQuery {

    private final LearningContextRepository versions;
    private final LearningEnrollmentRepository enrollments;
    private final LearningPathJpaRepository paths;
    private final IdentityAccessQuery identities;

    public EnrollmentLookupQueryService(
            LearningContextRepository versions,
            LearningEnrollmentRepository enrollments,
            LearningPathJpaRepository paths,
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
                .orElseThrow(() -> new IllegalArgumentException("Learning path version is not owned by organization"));
        var version = versions.findById(pathVersionId)
                .orElseThrow(() -> new IllegalArgumentException("Learning path version not found"));
        var path = paths.findById(version.getPathId()).orElse(null);
        String normalized = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        int pageSize = Math.min(Math.max(size, 1), 100);
        return enrollments.searchByVersionId(pathVersionId, normalized, PageRequest.of(Math.max(0, page), pageSize)).map(
                enrollment -> toSummary(enrollment, context, version.getVersionNo(),
                        path == null ? "" : path.getTitle()));
    }

    private EnrollmentSummary toSummary(
            EnrollmentEntity enrollment,
            LearningContextRepository.VersionProjection context,
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
