package com.skillproof.backend.feedback.api;

import com.skillproof.backend.feedback.application.FeedbackService;
import com.skillproof.backend.feedback.infrastructure.*;

import jakarta.validation.*;
import jakarta.validation.constraints.*;

import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1")
public class FeedbackController {
    private final FeedbackService service;

    public FeedbackController(FeedbackService service) {
        this.service = service;
    }

    public record ReviewInput(
            @Min(1) @Max(5) int rating, @NotBlank @Size(max = 4000) String body) {}

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
    public LearnerReview write(
            Authentication a,
            @PathVariable String type,
            @PathVariable UUID target,
            @Valid @RequestBody ReviewInput i) {
        return service.write((UUID) a.getPrincipal(), type, target, i.rating(), i.body());
    }

    @GetMapping("/organizations/{org}/feedback")
    public Page<LearnerReview> org(
            Authentication a, @PathVariable UUID org, @RequestParam(defaultValue = "0") int page) {
        return service.organization((UUID) a.getPrincipal(), org, page);
    }

    @PostMapping("/feedback/{id}/reply")
    public LearnerReview reply(
            Authentication a, @PathVariable UUID id, @Valid @RequestBody TextInput i) {
        return service.reply((UUID) a.getPrincipal(), id, i.body());
    }

    @PostMapping("/feedback/{id}/reports")
    public ContentReport report(
            Authentication a, @PathVariable UUID id, @Valid @RequestBody TextInput i) {
        return service.report((UUID) a.getPrincipal(), id, i.body());
    }

    @GetMapping("/admin/feedback/reviews/{id}")
    public LearnerReview detail(Authentication a, @PathVariable UUID id) {
        return service.adminReview((UUID) a.getPrincipal(), id);
    }

    @GetMapping("/admin/feedback/reports")
    public Page<ContentReport> reports(
            Authentication a, @RequestParam(defaultValue = "0") int page) {
        return service.reports((UUID) a.getPrincipal(), page);
    }

    @PostMapping("/admin/feedback/reports/{id}/resolve")
    public void resolve(
            Authentication a, @PathVariable UUID id, @Valid @RequestBody ModerationInput i) {
        service.resolve((UUID) a.getPrincipal(), id, i.hide(), i.reason());
    }

    @GetMapping("/feedback/{id}/moderation-history")
    public java.util.List<com.skillproof.backend.feedback.infrastructure.ReviewModerationEntry>
            history(Authentication a, @PathVariable UUID id) {
        return service.moderationHistory((UUID) a.getPrincipal(), id);
    }
}
