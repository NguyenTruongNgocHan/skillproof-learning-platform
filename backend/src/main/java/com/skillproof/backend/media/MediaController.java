package com.skillproof.backend.media;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/media")
public class MediaController {

    private final MediaService service;

    public MediaController(MediaService service) {
        this.service = service;
    }

    private UUID actor(Authentication auth) {
        return (UUID) auth.getPrincipal();
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)

    public Map<String, Object> upload(Authentication auth, @RequestParam String scope, @RequestParam(required = false) UUID target, @RequestPart("file") MultipartFile file) {
        return service.upload(actor(auth), scope, target, file);
    }

    @GetMapping
    public List<Map<String, Object>> list(Authentication auth, @RequestParam String scope, @RequestParam(required = false) UUID target) {
        return service.list(actor(auth), scope, target);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InputStreamResource> download(Authentication auth, @PathVariable UUID id) {
        var d = service.download(actor(auth), id);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(d.mime()))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(d.filename(), StandardCharsets.UTF_8).build().toString())
                .header("X-Content-Type-Options", "nosniff")
                .header(HttpHeaders.CACHE_CONTROL, "private, no-store")
                .contentLength(d.size()).body(new InputStreamResource(service.open(d.key())));
    }
}
