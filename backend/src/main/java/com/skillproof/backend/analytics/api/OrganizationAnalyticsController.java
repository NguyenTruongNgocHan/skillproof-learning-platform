package com.skillproof.backend.analytics.api;

import com.skillproof.backend.analytics.application.*;

import jakarta.validation.*;
import jakarta.validation.constraints.*;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.util.*;

@RestController
@RequestMapping("/api/v1")
public class OrganizationAnalyticsController {
    private final MarketingCaptureService capture;
    private final OrganizationAnalyticsService analytics;

    public OrganizationAnalyticsController(
            MarketingCaptureService capture, OrganizationAnalyticsService analytics) {
        this.capture = capture;
        this.analytics = analytics;
    }

    public record EventInput(
            @NotNull UUID organizationId,
            UUID versionId,
            @NotNull UUID visitorId,
            @NotNull UUID requestId,
            @NotBlank String kind,
            @NotNull @Size(max = 120) String source,
            @NotNull @Size(max = 120) String campaign) {}

    @PostMapping("/analytics/events")
    public void event(Authentication a, @Valid @RequestBody EventInput i) {
        capture.capture(
                a != null && a.getPrincipal() instanceof UUID ? (UUID) a.getPrincipal() : null,
                i.organizationId(),
                i.versionId(),
                i.visitorId(),
                i.requestId(),
                i.kind(),
                i.source(),
                i.campaign());
    }

    @GetMapping("/organizations/{org}/analytics")
    public Map<String, Object> dashboard(
            Authentication a,
            @PathVariable UUID org,
            @RequestParam Instant start,
            @RequestParam Instant end,
            @RequestParam(defaultValue = "") String version,
            @RequestParam(defaultValue = "") String campaign) {
        return analytics.dashboard((UUID) a.getPrincipal(), org, start, end, version, campaign);
    }
}
