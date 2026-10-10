package com.skillproof.backend.course.api;

import java.util.Map;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.skillproof.backend.course.application.CourseAuthoringService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/v1/admin/course-versions")
public class AdminCourseReviewController {

    private final CourseAuthoringService service;

    public AdminCourseReviewController(CourseAuthoringService service) {
        this.service = service;
    }

    @GetMapping
    public java.util.List<com.skillproof.backend.course.infrastructure.CourseVersionEntity> queue(Authentication auth, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return service.reviewQueue((UUID) auth.getPrincipal(), page, size);
    }

    public record ReviewInput(boolean approved, @NotBlank
            @Size(max = 1000) String reason) {

    }

    @GetMapping("/{id}/outline")
    public Map<String, Object> outline(Authentication auth, @PathVariable UUID id) {
        return service.reviewOutline((UUID) auth.getPrincipal(), id);
    }

    @PostMapping("/{id}/review")
    public Map<String, Object> review(Authentication auth, @PathVariable UUID id, @Valid @RequestBody ReviewInput input) {
        return service.review((UUID) auth.getPrincipal(), id, input.approved(), input.reason());
    }
}
