package com.skillproof.backend.library.api;

import com.skillproof.backend.library.api.model.LibraryResourceResponse;
import com.skillproof.backend.library.application.LibraryResourceService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/library")
public class LibraryResourceController {

  private final LibraryResourceService service;

  public LibraryResourceController(LibraryResourceService service) {
    this.service = service;
  }

  public record ResourceInput(
      @NotBlank @Size(max = 180) String title,
      @NotBlank @Size(max = 1000) String summary,
      @NotNull @Size(max = 100000) String body,
      @PositiveOrZero long priceVnd) {}

  @GetMapping
  public List<LibraryResourceService.Summary> discover(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
    return service.discover(page, size);
  }

  @GetMapping("/{id}")
  public LibraryResourceService.Summary detail(@PathVariable UUID id) {
    return service.detail(id);
  }

  @GetMapping("/{id}/content")
  public LibraryResourceResponse content(Authentication auth, @PathVariable UUID id) {
    return LibraryResourceResponse.from(service.content((UUID) auth.getPrincipal(), id));
  }

  @GetMapping("/me/contributions")
  public List<LibraryResourceResponse> mine(Authentication auth) {
    return service.mine((UUID) auth.getPrincipal()).stream()
        .map(LibraryResourceResponse::from)
        .toList();
  }

  @PostMapping
  public LibraryResourceResponse create(Authentication auth, @Valid @RequestBody ResourceInput i) {
    return LibraryResourceResponse.from(
        service.create((UUID) auth.getPrincipal(), i.title(), i.summary(), i.body(), i.priceVnd()));
  }

  @PutMapping("/{id}")
  public LibraryResourceResponse edit(
      Authentication auth, @PathVariable UUID id, @Valid @RequestBody ResourceInput i) {
    return LibraryResourceResponse.from(
        service.edit(
            (UUID) auth.getPrincipal(), id, i.title(), i.summary(), i.body(), i.priceVnd()));
  }

  @PostMapping("/{id}/submit-review")
  public LibraryResourceResponse submit(Authentication auth, @PathVariable UUID id) {
    return LibraryResourceResponse.from(service.submit((UUID) auth.getPrincipal(), id));
  }

  public record OrganizationResourceInput(
      @NotBlank @Size(max = 180) String title,
      @NotBlank @Size(max = 1000) String summary,
      @NotNull @Size(max = 100000) String body,
      @PositiveOrZero long priceVnd,
      @NotBlank @Pattern(regexp = "PUBLIC|RESTRICTED") String accessMode) {}

  @PostMapping("/organizations/{organization}")
  public LibraryResourceResponse organizationCreate(
      Authentication auth,
      @PathVariable UUID organization,
      @Valid @RequestBody OrganizationResourceInput i) {
    return LibraryResourceResponse.from(
        service.createOrganization(
            (UUID) auth.getPrincipal(),
            organization,
            i.title(),
            i.summary(),
            i.body(),
            i.priceVnd(),
            i.accessMode()));
  }

  @GetMapping("/organizations/{organization}")
  public List<LibraryResourceResponse> organizationList(
      Authentication auth, @PathVariable UUID organization) {
    return service.organizationResources((UUID) auth.getPrincipal(), organization).stream()
        .map(LibraryResourceResponse::from)
        .toList();
  }
}
