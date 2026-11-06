package com.skillproof.backend.library.api;

import com.skillproof.backend.library.api.model.LibraryResourceResponse;
import com.skillproof.backend.library.application.LibraryResourceService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/library")
public class AdminLibraryReviewController {

  private final LibraryResourceService service;

  public AdminLibraryReviewController(LibraryResourceService service) {
    this.service = service;
  }

  @GetMapping
  public java.util.List<LibraryResourceResponse> queue(
      Authentication auth,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return service.reviewQueue((UUID) auth.getPrincipal(), page, size).stream()
        .map(LibraryResourceResponse::from)
        .toList();
  }

  @GetMapping("/{id}")
  public LibraryResourceResponse detail(Authentication auth, @PathVariable UUID id) {
    return LibraryResourceResponse.from(service.reviewContent((UUID) auth.getPrincipal(), id));
  }

  public record ReviewInput(boolean approved, @NotBlank @Size(max = 1000) String reason) {}

  @PostMapping("/{id}/review")
  public LibraryResourceResponse review(
      Authentication auth, @PathVariable UUID id, @Valid @RequestBody ReviewInput i) {
    return LibraryResourceResponse.from(
        service.review((UUID) auth.getPrincipal(), id, i.approved(), i.reason()));
  }
}
