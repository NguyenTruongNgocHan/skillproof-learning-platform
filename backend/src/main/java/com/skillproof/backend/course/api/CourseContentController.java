package com.skillproof.backend.course.api;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.skillproof.backend.course.application.CourseContentQueryService;

@RestController
@RequestMapping("/api/v1/courses/me/enrollments")
public class CourseContentController {

    private final CourseContentQueryService service;

    public CourseContentController(CourseContentQueryService service) {
        this.service = service;
    }

    @GetMapping("/{id}/content")
    public List<CourseContentQueryService.Module> content(Authentication auth, @PathVariable UUID id) {
        return service.enrolled((UUID) auth.getPrincipal(), id);
    }
}
