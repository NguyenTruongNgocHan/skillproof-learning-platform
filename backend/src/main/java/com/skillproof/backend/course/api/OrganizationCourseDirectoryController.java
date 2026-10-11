package com.skillproof.backend.course.api;

import com.skillproof.backend.course.application.OrganizationCourseDirectoryService;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/course-workspace/organizations/{org}")
public class OrganizationCourseDirectoryController {
    private final OrganizationCourseDirectoryService directory;

    public OrganizationCourseDirectoryController(OrganizationCourseDirectoryService directory) {
        this.directory = directory;
    }

    @GetMapping("/versions")
    public List<OrganizationCourseDirectoryService.Version> versions(
            Authentication a, @PathVariable UUID org) {
        return directory.versions((UUID) a.getPrincipal(), org);
    }
}
