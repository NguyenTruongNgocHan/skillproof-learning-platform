package com.skillproof.backend.assignment.application;

import com.skillproof.backend.assignment.infrastructure.persistence.*;
import com.skillproof.backend.course.contract.CourseWorkspaceQuery;
import com.skillproof.backend.organization.contract.OrganizationAuthorityQuery;

import org.springframework.data.domain.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class OrganizationGradingService {
    @org.springframework.beans.factory.annotation.Autowired
    private AssignmentSubmissionRepository submissions;

    @org.springframework.beans.factory.annotation.Autowired
    private com.skillproof.backend.identity.contract.IdentityAccessQuery identity;

    private final AssignmentRepository assignments;
    private final CourseWorkspaceQuery courses;
    private final OrganizationAuthorityQuery grants;

    public OrganizationGradingService(
            AssignmentRepository assignments,
            CourseWorkspaceQuery courses,
            OrganizationAuthorityQuery grants) {
        this.assignments = assignments;
        this.courses = courses;
        this.grants = grants;
    }

    @Transactional(readOnly = true)
    public Page<AssignmentEntity> list(UUID actor, UUID org, int page) {
        if (!grants.hasAuthority(org, actor, "GRADE_ASSIGNMENTS"))
            throw new AccessDeniedException("Grading authority required");
        var versions =
                courses.versions(org).stream()
                        .filter(v -> !"DRAFT".equals(courses.context(v).status()))
                        .toList();
        if (versions.isEmpty()) return Page.empty();
        return assignments.findByVersionIdIn(
                versions, PageRequest.of(Math.max(0, page), 25, Sort.by("title")));
    }

    public record QueueItem(
            AssignmentEntity assignment, AssignmentSubmissionEntity submission, String email) {}

    @Transactional(readOnly = true)
    public Page<QueueItem> queue(UUID actor, UUID organization, String status, int page) {
        if (!grants.hasAuthority(organization, actor, "GRADE_ASSIGNMENTS"))
            throw new AccessDeniedException("Grading authority required");
        if (!java.util.Set.of("SUBMITTED", "GRADED").contains(status))
            throw new com.skillproof.backend.common.exception.BadRequestException(
                    "GRADING_FILTER", "Use SUBMITTED or GRADED");
        var versions =
                courses.versions(organization).stream()
                        .filter(v -> !"DRAFT".equals(courses.context(v).status()))
                        .toList();
        if (versions.isEmpty()) return Page.empty();
        return submissions
                .queue(versions, status, PageRequest.of(Math.max(0, page), 25))
                .map(
                        s ->
                                new QueueItem(
                                        assignments.findById(s.getAssignmentId()).orElseThrow(),
                                        s,
                                        identity.find(s.getLearnerId())
                                                .map(
                                                        com.skillproof.backend.identity.contract
                                                                        .IdentityAccessQuery.Account
                                                                ::email)
                                                .orElse("")));
    }
}
