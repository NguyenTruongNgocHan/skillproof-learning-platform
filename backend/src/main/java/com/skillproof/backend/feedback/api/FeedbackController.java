package com.skillproof.backend.feedback.api;

import com.skillproof.backend.feedback.api.model.ContentReportResponse;
import com.skillproof.backend.feedback.api.model.LearnerReviewResponse;
import com.skillproof.backend.feedback.api.model.ReviewModerationEntryResponse;
import com.skillproof.backend.feedback.application.FeedbackService;
import com.skillproof.backend.feedback.infrastructure.*;
import jakarta.validation.*;
import jakarta.validation.constraints.*;
import java.util.*;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class FeedbackController {
  private final FeedbackService service;

  public FeedbackController(FeedbackService service) {
    this.service = service;
  }

  public record ReviewInput(@Min(1) @Max(5) int rating, @NotBlank @Size(max = 4000) String body) {}

  public record TextInput(@NotBlank @Size(max = 2000) String body) {}

  public record ModerationInput(boolean hide, @NotBlank @Size(max = 1000) String reason) {}

  @GetMapping("/public/feedback/{type}/{target}")
  public Map<String, Object> list(
      @PathVariable String type,
      @PathVariable UUID target,
      @RequestParam(defaultValue = "0") int page) {
    return service.publicList(type, target, page);
  }

  @PutMapping("/feedback/{type}/{target}")
  public LearnerReviewResponse write(
      Authentication a,
      @PathVariable String type,
      @PathVariable UUID target,
      @Valid @RequestBody ReviewInput i) {
    return LearnerReviewResponse.from(
        service.write((UUID) a.getPrincipal(), type, target, i.rating(), i.body()));
  }

  @GetMapping("/organizations/{org}/feedback")
  public Page<LearnerReviewResponse> org(
      Authentication a, @PathVariable UUID org, @RequestParam(defaultValue = "0") int page) {
    return service
        .organization((UUID) a.getPrincipal(), org, page)
        .map(LearnerReviewResponse::from);
  }

  @PostMapping("/feedback/{id}/reply")
  public LearnerReviewResponse reply(
      Authentication a, @PathVariable UUID id, @Valid @RequestBody TextInput i) {
    return LearnerReviewResponse.from(service.reply((UUID) a.getPrincipal(), id, i.body()));
  }

  @PostMapping("/feedback/{id}/reports")
  public ContentReportResponse report(
      Authentication a, @PathVariable UUID id, @Valid @RequestBody TextInput i) {
    return ContentReportResponse.from(service.report((UUID) a.getPrincipal(), id, i.body()));
  }

  @GetMapping("/admin/feedback/reviews/{id}")
  public LearnerReviewResponse detail(Authentication a, @PathVariable UUID id) {
    return LearnerReviewResponse.from(service.adminReview((UUID) a.getPrincipal(), id));
  }

  @GetMapping("/admin/feedback/reports")
  public Page<ContentReportResponse> reports(
      Authentication a, @RequestParam(defaultValue = "0") int page) {
    return service.reports((UUID) a.getPrincipal(), page).map(ContentReportResponse::from);
  }

  @PostMapping("/admin/feedback/reports/{id}/resolve")
  public void resolve(
      Authentication a, @PathVariable UUID id, @Valid @RequestBody ModerationInput i) {
    service.resolve((UUID) a.getPrincipal(), id, i.hide(), i.reason());
  }

  @GetMapping("/feedback/{id}/moderation-history")
  public java.util.List<ReviewModerationEntryResponse> history(
      Authentication a, @PathVariable UUID id) {
    return service.moderationHistory((UUID) a.getPrincipal(), id).stream()
        .map(ReviewModerationEntryResponse::from)
        .toList();
  }
}
