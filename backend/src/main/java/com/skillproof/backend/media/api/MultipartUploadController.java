package com.skillproof.backend.media.api;

import com.skillproof.backend.media.application.MultipartUploadService;

import jakarta.validation.Valid;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/media/multipart")
public class MultipartUploadController {
    private final MultipartUploadService uploads;

    public MultipartUploadController(MultipartUploadService uploads) {
        this.uploads = uploads;
    }

    @PostMapping
    public Map<String, Object> prepare(
            Authentication a, @Valid @RequestBody DirectUploadController.UploadInput i) {
        return uploads.prepare(
                (UUID) a.getPrincipal(), i.scope(), i.target(), i.filename(), i.size());
    }

    @GetMapping("/{id}")
    public Map<String, Object> status(Authentication a, @PathVariable UUID id) {
        return uploads.status((UUID) a.getPrincipal(), id);
    }

    @PostMapping("/{id}/parts/{number}")
    public Map<String, String> part(
            Authentication a, @PathVariable UUID id, @PathVariable int number) {
        return uploads.part((UUID) a.getPrincipal(), id, number);
    }

    @PostMapping("/{id}/assemble")
    public void assemble(Authentication a, @PathVariable UUID id) {
        uploads.assemble((UUID) a.getPrincipal(), id);
    }

    @DeleteMapping("/{id}")
    public void cancel(Authentication a, @PathVariable UUID id) {
        uploads.cancel((UUID) a.getPrincipal(), id);
    }
}
