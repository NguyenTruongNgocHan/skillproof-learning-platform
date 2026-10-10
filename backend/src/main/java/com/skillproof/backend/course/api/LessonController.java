package com.skillproof.backend.course.api;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.skillproof.backend.course.application.LessonService;
import com.skillproof.backend.course.infrastructure.persistence.LessonEntity;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/v1/courses")
public class LessonController {

    private final LessonService service;

    public LessonController(LessonService service) {
        this.service = service;
    }

    public record LessonInput(@Min(1) int position, @NotBlank
            @Size(max = 180) String title, @NotNull
            @Size(max = 100000) String body) {

    }

    public record ResourceInput(@Min(1) int position, @NotBlank
            @Pattern(regexp = "ARTICLE|LINK|VIDEO|FILE|AUDIO|IMAGE") String kind,
            @NotBlank
            @Size(max = 180) String title, @Size(max = 100000) String body, @Size(max = 1000) String url, boolean required, boolean preview) {

    }

    @PostMapping("/modules/{module}/lessons")
    public LessonEntity create(Authentication auth, @PathVariable UUID module, @Valid @RequestBody LessonInput i) {
        return service.create((UUID) auth.getPrincipal(), module, i.position(), i.title(), i.body());
    }

    @GetMapping("/modules/{module}/lessons")
    public List<LessonEntity> list(Authentication auth, @PathVariable UUID module) {
        return service.authorList((UUID) auth.getPrincipal(), module);
    }

    @PatchMapping("/lessons/{id}")
    public LessonEntity edit(Authentication auth, @PathVariable UUID id, @Valid @RequestBody LessonInput i) {
        return service.edit((UUID) auth.getPrincipal(), id, i.position(), i.title(), i.body());
    }

    @DeleteMapping("/lessons/{id}")
    @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
    public void delete(Authentication auth, @PathVariable UUID id) {
        service.delete((UUID) auth.getPrincipal(), id);
    }

    @PostMapping("/lessons/{id}/resources")
    public Map<String, Object> resource(Authentication auth, @PathVariable UUID id, @Valid @RequestBody ResourceInput i) {
        return service.resource((UUID) auth.getPrincipal(), id, i.position(), i.kind(), i.title(), i.body(), i.url(), i.required(), i.preview());
    }
}
