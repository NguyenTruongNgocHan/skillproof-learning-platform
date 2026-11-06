package com.skillproof.backend.course.api;

import com.skillproof.backend.course.application.CourseMetadataService;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/courses/versions/{id}/metadata")
public class CourseMetadataController {
  private final CourseMetadataService service;

  public CourseMetadataController(CourseMetadataService service) {
    this.service = service;
  }

  @GetMapping
  public CourseMetadataService.Metadata get(Authentication auth, @PathVariable UUID id) {
    return service.get((UUID) auth.getPrincipal(), id);
  }

  @PutMapping
  public CourseMetadataService.Metadata save(
      Authentication auth,
      @PathVariable UUID id,
      @RequestBody CourseMetadataService.Metadata input) {
    return service.save((UUID) auth.getPrincipal(), id, input);
  }
}
