package com.skillproof.backend.course.api;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.skillproof.backend.course.application.CourseOrderingService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/v1/courses")
public class CourseOrderingController {

    private final CourseOrderingService service;

    public CourseOrderingController(CourseOrderingService service) {
        this.service = service;
    }

    public record Order(@NotNull
            @Size(max = 1000) List<@NotNull UUID> ids) {

    }

    @PutMapping("/versions/{id}/module-order")
    public void modules(Authentication auth, @PathVariable UUID id, @Valid @RequestBody Order i) {
        service.modules((UUID) auth.getPrincipal(), id, i.ids());
    }

    @PutMapping("/modules/{id}/lesson-order")
    public void lessons(Authentication auth, @PathVariable UUID id, @Valid @RequestBody Order i) {
        service.lessons((UUID) auth.getPrincipal(), id, i.ids());
    }

    @PutMapping("/lessons/{id}/resource-order")
    public void resources(Authentication auth, @PathVariable UUID id, @Valid @RequestBody Order i) {
        service.resources((UUID) auth.getPrincipal(), id, i.ids());
    }
}
