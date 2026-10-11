package com.skillproof.backend.course.api;

import com.skillproof.backend.course.application.OrganizerEnrollmentService;

import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/course-workspace")
public class OrganizerEnrollmentController {
    private final OrganizerEnrollmentService service;

    public OrganizerEnrollmentController(OrganizerEnrollmentService service) {
        this.service = service;
    }

    @GetMapping("/versions/{id}/enrollments")
    public Page<Map<String, Object>> list(
            Authentication a,
            @PathVariable UUID id,
            @RequestParam(defaultValue = "") String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "") String status,
            @RequestParam(defaultValue = "1970-01-01T00:00:00Z") java.time.Instant start,
            @RequestParam(defaultValue = "9999-12-31T00:00:00Z") java.time.Instant end) {
        return service.list((UUID) a.getPrincipal(), id, query, page, status, start, end);
    }

    @GetMapping("/versions/{id}/enrollments/{enrollment}")
    public Map<String, Object> detail(
            Authentication a, @PathVariable UUID id, @PathVariable UUID enrollment) {
        return service.detail((UUID) a.getPrincipal(), id, enrollment);
    }
}
