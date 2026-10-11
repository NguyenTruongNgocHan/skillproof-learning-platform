package com.skillproof.backend.course.api;

import com.skillproof.backend.course.application.CourseCatalogService;

import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/public/organizations")
public class PublicOrganizationCoursesController {
    private final CourseCatalogService service;

    public PublicOrganizationCoursesController(CourseCatalogService service) {
        this.service = service;
    }

    @GetMapping("/{org}/courses")
    public List<CourseCatalogService.Summary> list(
            @PathVariable UUID org,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return service.organization(org, page, size);
    }
}
