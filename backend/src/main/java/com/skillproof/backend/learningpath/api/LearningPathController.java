package com.skillproof.backend.learningpath.api;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.skillproof.backend.learningpath.application.LearningPathService;
import com.skillproof.backend.learningpath.infrastructure.persistence.LearningPathEntity;
import com.skillproof.backend.learningpath.infrastructure.persistence.LearningPathItemEntity;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/v1/learning-paths")
public class LearningPathController {

    private final LearningPathService service;

    public LearningPathController(LearningPathService service) {
        this.service = service;
    }

    public record PathInput(@NotBlank
            @Size(max = 180) String title, @NotNull
            @Size(max = 1000) String goal) {

    }

    public record ItemInput(@NotNull UUID courseVersionId, @Min(1) int position, UUID prerequisiteItemId) {

    }

    @PostMapping
    public LearningPathEntity create(Authentication auth, @Valid @RequestBody PathInput i) {
        return service.create((UUID) auth.getPrincipal(), i.title(), i.goal());
    }

    @GetMapping
    public List<LearningPathEntity> mine(Authentication auth) {
        return service.mine((UUID) auth.getPrincipal());
    }

    @PostMapping("/{id}/courses")
    public LearningPathItemEntity add(Authentication auth, @PathVariable UUID id, @Valid @RequestBody ItemInput i) {
        return service.add((UUID) auth.getPrincipal(), id, i.courseVersionId(), i.position(), i.prerequisiteItemId());
    }

    @GetMapping("/{id}/outline")
    public List<LearningPathService.Item> outline(Authentication auth, @PathVariable UUID id) {
        return service.outline((UUID) auth.getPrincipal(), id);
    }

    @DeleteMapping("/{id}/courses/{item}")
    @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
    public void remove(Authentication auth, @PathVariable UUID id, @PathVariable UUID item) {
        service.remove((UUID) auth.getPrincipal(), id, item);
    }
}
