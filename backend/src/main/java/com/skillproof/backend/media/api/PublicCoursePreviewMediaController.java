package com.skillproof.backend.media.api;

import com.skillproof.backend.media.application.MediaService;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/public/course-previews")
public class PublicCoursePreviewMediaController {
  private final MediaService media;

  public PublicCoursePreviewMediaController(MediaService media) {
    this.media = media;
  }

  @GetMapping("/{resource}/files")
  public List<Map<String, Object>> files(@PathVariable UUID resource) {
    return media.publicPreviewFiles(resource);
  }

  @GetMapping("/{resource}/files/{id}")
  public ResponseEntity<InputStreamResource> file(
      @PathVariable UUID resource, @PathVariable UUID id) {
    var file = media.publicPreview(resource, id);
    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(file.mime()))
        .contentLength(file.size())
        .header("X-Content-Type-Options", "nosniff")
        .header(HttpHeaders.CACHE_CONTROL, "no-store")
        .header(
            HttpHeaders.CONTENT_DISPOSITION,
            ContentDisposition.attachment()
                .filename(file.filename(), StandardCharsets.UTF_8)
                .build()
                .toString())
        .body(new InputStreamResource(media.open(file.key())));
  }
}
