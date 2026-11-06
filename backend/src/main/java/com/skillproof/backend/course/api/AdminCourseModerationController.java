package com.skillproof.backend.course.api;

import com.skillproof.backend.course.application.CourseModerationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/courses")
public class AdminCourseModerationController {
  private final CourseModerationService service;

  public AdminCourseModerationController(CourseModerationService service) {
    this.service = service;
  }

  public record Input(boolean suspended, @NotBlank @Size(max = 1000) String reason) {}

  @PostMapping("/{id}/moderation")
  public CourseModerationService.Decision moderate(
      Authentication auth, @PathVariable UUID id, @Valid @RequestBody Input input) {
    return service.moderate((UUID) auth.getPrincipal(), id, input.suspended(), input.reason());
  }

  @GetMapping("/search")
  public com.skillproof.backend.common.api.PageResult<CourseModerationService.Summary> search(
      Authentication auth,
      @RequestParam(defaultValue = "") String query,
      @RequestParam(required = false) Boolean suspended,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return service.search((UUID) auth.getPrincipal(), query, suspended, page, size);
  }

  @GetMapping("/{id}")
  public CourseModerationService.Summary detail(Authentication auth, @PathVariable UUID id) {
    return service.detail((UUID) auth.getPrincipal(), id);
  }
}
