package com.skillproof.backend.media.api;

import com.skillproof.backend.media.application.DirectUploadService;

import jakarta.validation.*;
import jakarta.validation.constraints.*;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/media/direct")
public class DirectUploadController {
    private final DirectUploadService service;

    public DirectUploadController(DirectUploadService service) {
        this.service = service;
    }

    public record UploadInput(
            @NotBlank String scope,
            UUID target,
            @NotBlank @Size(max = 180) String filename,
            @Min(1) @Max(524288000) long size) {}

    @PostMapping
    public Map<String, Object> prepare(Authentication a, @Valid @RequestBody UploadInput i) {
        return service.prepare(
                (UUID) a.getPrincipal(), i.scope(), i.target(), i.filename(), i.size());
    }

    @PostMapping("/{id}/complete")
    public Map<String, Object> complete(Authentication a, @PathVariable UUID id) {
        return service.complete((UUID) a.getPrincipal(), id);
    }
}
