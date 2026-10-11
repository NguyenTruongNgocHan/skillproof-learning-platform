package com.skillproof.backend.course.api;

import com.skillproof.backend.course.application.CourseReadinessService;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/course-workspace")
public class CourseReadinessController {
    private final CourseReadinessService service;

    public CourseReadinessController(CourseReadinessService service) {
        this.service = service;
    }

    @GetMapping("/versions/{id}/readiness")
    public Map<String, Object> get(Authentication a, @PathVariable UUID id) {
        return service.check((UUID) a.getPrincipal(), id);
    }
}
