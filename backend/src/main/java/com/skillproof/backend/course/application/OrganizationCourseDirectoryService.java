package com.skillproof.backend.course.application;

import com.skillproof.backend.course.contract.CourseWorkspaceQuery;
import com.skillproof.backend.organization.contract.OrganizationAuthorityQuery;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class OrganizationCourseDirectoryService {
    private final CourseWorkspaceQuery courses;
    private final OrganizationAuthorityQuery grants;

    public OrganizationCourseDirectoryService(
            CourseWorkspaceQuery courses, OrganizationAuthorityQuery grants) {
        this.courses = courses;
        this.grants = grants;
    }

    public record Version(UUID id, UUID courseId, String title, String status) {}

    @Transactional(readOnly = true)
    public List<Version> versions(UUID actor, UUID org) {
        if (java.util.stream.Stream.of(
                        "MANAGE_CONTENT",
                        "MANAGE_PRICING",
                        "VIEW_ANALYTICS",
                        "ISSUE_CERTIFICATES",
                        "MANAGE_PROFILE")
                .noneMatch(g -> grants.hasAuthority(org, actor, g)))
            throw new org.springframework.security.access.AccessDeniedException(
                    "Workspace course authority required");
        return courses.versions(org).stream()
                .map(courses::context)
                .filter(c -> "PUBLISHED".equals(c.status()))
                .map(c -> new Version(c.versionId(), c.courseId(), c.title(), c.status()))
                .toList();
    }
}
