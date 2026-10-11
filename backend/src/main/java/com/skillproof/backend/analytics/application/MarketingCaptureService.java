package com.skillproof.backend.analytics.application;

import com.skillproof.backend.analytics.contract.MarketingJournal;
import com.skillproof.backend.analytics.infrastructure.*;
import com.skillproof.backend.common.exception.*;
import com.skillproof.backend.course.contract.CourseWorkspaceQuery;
import com.skillproof.backend.organization.contract.*;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;

@Service
public class MarketingCaptureService implements MarketingJournal {
    private final MarketingEventRepository events;
    private final CourseWorkspaceQuery courses;
    private final OrganizationPublicQuery organizations;
    private final OrganizationAuthorityQuery grants;
    private final StringRedisTemplate redis;

    public MarketingCaptureService(
            MarketingEventRepository events,
            CourseWorkspaceQuery courses,
            OrganizationPublicQuery organizations,
            OrganizationAuthorityQuery grants,
            StringRedisTemplate redis) {
        this.events = events;
        this.courses = courses;
        this.organizations = organizations;
        this.grants = grants;
        this.redis = redis;
    }

    @Transactional
    public void capture(
            UUID actor,
            UUID org,
            UUID version,
            UUID visitor,
            UUID request,
            String kind,
            String source,
            String campaign) {
        if (!Set.of("IMPRESSION", "DETAIL_VIEW", "ORG_VIEW").contains(kind))
            throw new BadRequestException("EVENT_KIND", "Client event not permitted");
        if (version != null) {
            var c = courses.context(version);
            if (!"PUBLISHED".equals(c.status()) || !org.equals(c.organizationId()))
                throw new BadRequestException(
                        "EVENT_TARGET", "Published organization course required");
            if (actor != null && (actor.equals(c.author()) || grants.activeMember(org, actor)))
                return;
        } else if (!"ORG_VIEW".equals(kind))
            throw new BadRequestException("EVENT_TARGET", "Course version required");
        if (!organizations
                .find(org)
                .map(OrganizationPublicQuery.OrganizationView::approved)
                .orElse(false))
            throw new BadRequestException("EVENT_TARGET", "Approved organization required");
        if (actor != null && grants.activeMember(org, actor)) return;
        String key =
                "analytics:rate:"
                        + (actor == null ? visitor : actor)
                        + ":"
                        + (Instant.now().getEpochSecond() / 60);
        Long count = redis.opsForValue().increment(key);
        if (count != null && count == 1) redis.expire(key, Duration.ofMinutes(2));
        if (count == null || count > 120)
            throw new BadRequestException("EVENT_RATE", "Too many events");
        UUID id =
                UUID.nameUUIDFromBytes(
                        ("client:" + visitor + ":" + request)
                                .getBytes(java.nio.charset.StandardCharsets.UTF_8));
        if (events.existsById(id)) return;
        events.save(
                new MarketingEvent(
                        id,
                        org,
                        version,
                        actor,
                        actor == null ? visitor : actor,
                        kind,
                        source,
                        campaign,
                        Instant.now()));
    }

    @Transactional
    public void conversion(UUID actor, UUID version, String kind, UUID reference) {
        if (!Set.of("ENROLLMENT", "COMPLETION", "PAYMENT", "CHECKOUT", "LEARNING").contains(kind))
            throw new IllegalArgumentException("Server conversion kind");
        var c = courses.context(version);
        if (c.organizationId() == null) return;
        if (actor != null
                && (actor.equals(c.author()) || grants.activeMember(c.organizationId(), actor)))
            return;
        UUID id =
                UUID.nameUUIDFromBytes(
                        (kind + ":" + reference).getBytes(java.nio.charset.StandardCharsets.UTF_8));
        if (events.existsById(id)) return;
        var touch =
                events.findFirstByActorIdAndVersionIdAndKindOrderByCreatedAtDesc(
                                actor, version, "DETAIL_VIEW")
                        .filter(
                                e ->
                                        e.getCreatedAt()
                                                .isAfter(Instant.now().minus(Duration.ofDays(90))));
        events.save(
                new MarketingEvent(
                        id,
                        c.organizationId(),
                        version,
                        actor,
                        actor,
                        kind,
                        touch.map(MarketingEvent::getSource).orElse("server"),
                        touch.map(MarketingEvent::getCampaign).orElse(""),
                        Instant.now()));
    }
}
