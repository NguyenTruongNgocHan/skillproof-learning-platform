package com.skillproof.backend.assignment.api;

import com.skillproof.backend.assignment.api.model.AssignmentResponse;
import com.skillproof.backend.assignment.application.OrganizationGradingService;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/organizations/{org}/grading")
public class OrganizationGradingController {
  private final OrganizationGradingService service;

  public OrganizationGradingController(OrganizationGradingService service) {
    this.service = service;
  }

  @GetMapping
  public Page<AssignmentResponse> list(
      Authentication a, @PathVariable UUID org, @RequestParam(defaultValue = "0") int page) {
    return service.list((UUID) a.getPrincipal(), org, page).map(AssignmentResponse::from);
  }

  @GetMapping("/queue")
  public Page<QueueResponse> queue(
      Authentication a,
      @PathVariable UUID org,
      @RequestParam(defaultValue = "SUBMITTED") String status,
      @RequestParam(defaultValue = "0") int page) {
    return service
        .queue((UUID) a.getPrincipal(), org, status, page)
        .map(
            q ->
                new QueueResponse(
                    AssignmentResponse.from(q.assignment()),
                    com.skillproof.backend.assignment.api.model.AssignmentSubmissionResponse.from(
                        q.submission()),
                    q.email()));
  }

  public record QueueResponse(
      AssignmentResponse assignment,
      com.skillproof.backend.assignment.api.model.AssignmentSubmissionResponse submission,
      String email) {}
}
