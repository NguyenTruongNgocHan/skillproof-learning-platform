package com.skillproof.backend.course.api;

import com.skillproof.backend.course.application.CourseArtworkService;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1")
public class CourseArtworkController {
    private final CourseArtworkService service;

    public CourseArtworkController(CourseArtworkService service) {
        this.service = service;
    }

    public record Input(UUID coverMediaId) {}

    @GetMapping("/course-workspace/courses/{id}/artwork")
    public Map<String, Object> get(Authentication a, @PathVariable UUID id) {
        return service.read((UUID) a.getPrincipal(), id);
    }

    @PutMapping("/course-workspace/courses/{id}/artwork")
    public Map<String, Object> update(
            Authentication a, @PathVariable UUID id, @RequestBody Input i) {
        return service.update((UUID) a.getPrincipal(), id, i.coverMediaId());
    }

    @GetMapping("/public/courses/{id}/cover")
    public ResponseEntity<InputStreamResource> image(@PathVariable UUID id) {
        var f = service.image(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(f.mime()))
                .contentLength(f.size())
                .header("X-Content-Type-Options", "nosniff")
                .header("Cache-Control", "public, max-age=300")
                .body(new InputStreamResource(f.stream()));
    }
}
