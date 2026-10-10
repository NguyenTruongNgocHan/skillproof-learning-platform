package com.skillproof.backend.library.api;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.skillproof.backend.library.application.LibraryResourceService;
import com.skillproof.backend.library.infrastructure.persistence.LibraryResourceEntity;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/v1/admin/library")
public class AdminLibraryReviewController {

    private final LibraryResourceService service;

    public AdminLibraryReviewController(LibraryResourceService service) {
        this.service = service;
    }

    @GetMapping
    public java.util.List<LibraryResourceEntity> queue(Authentication auth, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return service.reviewQueue((UUID) auth.getPrincipal(), page, size);
    }

    @GetMapping("/{id}")
    public LibraryResourceEntity detail(Authentication auth, @PathVariable UUID id) {
        return service.reviewContent((UUID) auth.getPrincipal(), id);
    }

    public record ReviewInput(boolean approved, @NotBlank
            @Size(max = 1000) String reason) {

    }

    @PostMapping("/{id}/review")
    public LibraryResourceEntity review(Authentication auth, @PathVariable UUID id, @Valid @RequestBody ReviewInput i) {
        return service.review((UUID) auth.getPrincipal(), id, i.approved(), i.reason());
    }
}
