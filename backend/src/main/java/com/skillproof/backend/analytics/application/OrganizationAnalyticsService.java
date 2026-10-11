package com.skillproof.backend.analytics.application;

import com.skillproof.backend.analytics.infrastructure.*;
import com.skillproof.backend.common.exception.BadRequestException;
import com.skillproof.backend.organization.contract.OrganizationAuthorityQuery;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;

@Service
public class OrganizationAnalyticsService {
    @org.springframework.beans.factory.annotation.Autowired
    private com.skillproof.backend.payment.contract.OrganizationRevenueQuery revenue;

    @org.springframework.beans.factory.annotation.Autowired private MarketingAnalyticsReader reader;

    @org.springframework.beans.factory.annotation.Autowired
    private com.skillproof.backend.course.contract.CourseWorkspaceQuery courses;

    private final MarketingEventRepository events;
    private final OrganizationAuthorityQuery grants;

    public OrganizationAnalyticsService(
            MarketingEventRepository events, OrganizationAuthorityQuery grants) {
        this.events = events;
        this.grants = grants;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> dashboard(UUID actor, UUID org, Instant start, Instant end) {
        return dashboard(actor, org, start, end, "", "");
    }

    @Transactional(readOnly = true)
    public Map<String, Object> dashboard(
            UUID actor, UUID org, Instant start, Instant end, String version, String campaign) {
        if (!grants.hasAuthority(org, actor, "VIEW_ANALYTICS"))
            throw new AccessDeniedException("Analytics authority required");
        if (start == null
                || end == null
                || !end.isAfter(start)
                || Duration.between(start, end).compareTo(Duration.ofDays(93)) > 0)
            throw new BadRequestException("ANALYTICS_RANGE", "Choose at most 93 days");
        if (!version.isBlank()) {
            UUID id;
            try {
                id = UUID.fromString(version);
            } catch (IllegalArgumentException ex) {
                throw new BadRequestException("ANALYTICS_VERSION", "Invalid version");
            }
            if (!org.equals(courses.context(id).organizationId()))
                throw new AccessDeniedException("Organization course required");
        }
        if (campaign.length() > 120)
            throw new BadRequestException("ANALYTICS_CAMPAIGN", "Campaign too long");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("start", start);
        result.put("end", end);
        result.put("revenue", revenue.revenue(org, start, end));
        result.put("funnel", reader.funnel(org, start, end, version, campaign));
        result.put(
                "metrics",
                reader.metrics(org, start, end, version, campaign).stream()
                        .map(
                                x ->
                                        Map.of(
                                                "kind",
                                                x[0],
                                                "events",
                                                x[1],
                                                "visitors",
                                                x[2],
                                                "authenticatedAccounts",
                                                x[3]))
                        .toList());
        result.put(
                "daily",
                reader.daily(org, start, end, version, campaign).stream()
                        .map(x -> Map.of("date", x[0].toString(), "kind", x[1], "events", x[2]))
                        .toList());
        result.put(
                "sources",
                reader.sources(org, start, end, version, campaign).stream()
                        .map(
                                x ->
                                        Map.of(
                                                "source",
                                                x[0],
                                                "campaign",
                                                x[1],
                                                "kind",
                                                x[2],
                                                "events",
                                                x[3]))
                        .toList());
        result.put(
                "courses",
                reader.courses(org, start, end, version, campaign).stream()
                        .map(
                                x -> {
                                    Map<String, Object> m = new LinkedHashMap<>();
                                    m.put("versionId", x[0]);
                                    m.put(
                                            "title",
                                            x[0] == null
                                                    ? "Organization page"
                                                    : courses.context(
                                                                    UUID.fromString(
                                                                            x[0].toString()))
                                                            .title());
                                    m.put("kind", x[1]);
                                    m.put("events", x[2]);
                                    return m;
                                })
                        .toList());
        return result;
    }
}
