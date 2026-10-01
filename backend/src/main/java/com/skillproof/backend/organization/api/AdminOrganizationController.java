package com.skillproof.backend.organization.api;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.skillproof.backend.organization.application.OrganizationService;
import com.skillproof.backend.organization.domain.Organization;
import com.skillproof.backend.organization.domain.OrganizationReviewView;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/v1/admin/organizations")
@SecurityRequirement(name = "bearerAuth")
public class AdminOrganizationController {

    private final OrganizationService service;

    public AdminOrganizationController(OrganizationService service) {
        this.service = service;
    }

    public record ReviewRequest(@NotNull Organization.Status decision, @Size(max = 1000) String reason) {

    }

    public record ReviewResponse(@JsonProperty("reviewer_user_id") UUID reviewerUserId,
            String decision, String reason, @JsonProperty("reviewed_at") Instant reviewedAt) {

        static ReviewResponse from(OrganizationReviewView value) {
            return new ReviewResponse(value.reviewerUserId(), value.decision().name(),
                    value.reason(), value.reviewedAt());
        }
    }

    @GetMapping("/pending")
    public List<Organization> pending() {
        return service.pending();
    }

    @GetMapping("/{id}/reviews")
    public List<ReviewResponse> reviews(@PathVariable UUID id) {
        return service.reviews(id).stream().map(ReviewResponse::from).toList();
    }

    @PostMapping("/{id}/review")
    public Organization review(Authentication auth, @PathVariable UUID id, @Valid @RequestBody ReviewRequest r) {
        return service.review(id, (UUID) auth.getPrincipal(), new OrganizationService.Review(r.decision(), r.reason()));
    }
}
