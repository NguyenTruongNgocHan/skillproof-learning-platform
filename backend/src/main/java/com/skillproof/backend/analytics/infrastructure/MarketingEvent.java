package com.skillproof.backend.analytics.infrastructure;

import jakarta.persistence.*;

import java.time.*;
import java.util.*;

@Entity
@Table(name = "marketing_event")
public class MarketingEvent {
    @Id private UUID id;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "version_id")
    private UUID versionId;

    @Column(name = "actor_id")
    private UUID actorId;

    @Column(name = "visitor_id", nullable = false)
    private UUID visitorId;

    @Column(name = "kind", nullable = false, length = 30)
    private String kind;

    @Column(name = "source", nullable = false, length = 120)
    private String source;

    @Column(name = "campaign", nullable = false, length = 120)
    private String campaign;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected MarketingEvent() {}

    public MarketingEvent(
            UUID id,
            UUID organizationId,
            UUID versionId,
            UUID actorId,
            UUID visitorId,
            String kind,
            String source,
            String campaign,
            Instant createdAt) {
        this.id = id;
        this.organizationId = organizationId;
        this.versionId = versionId;
        this.actorId = actorId;
        this.visitorId = visitorId;
        this.kind = kind;
        this.source = source;
        this.campaign = campaign;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrganizationId() {
        return organizationId;
    }

    public UUID getVersionId() {
        return versionId;
    }

    public UUID getActorId() {
        return actorId;
    }

    public UUID getVisitorId() {
        return visitorId;
    }

    public String getKind() {
        return kind;
    }

    public String getSource() {
        return source;
    }

    public String getCampaign() {
        return campaign;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
