package com.skillproof.backend.recommendation.infrastructure;

import java.time.Instant;
import java.util.UUID;

import com.skillproof.backend.recommendation.domain.InteractionEventType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "recommendation_interaction_event")
public class InteractionEventEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 60)
    private InteractionEventType eventType;

    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    @Column(name = "entity_id")
    private UUID entityId;

    @Column(name = "recommendation_request_id")
    private UUID recommendationRequestId;

    @Column(name = "position")
    private Integer position;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    protected InteractionEventEntity() {
    }

    public InteractionEventEntity(
            UUID id,
            UUID userId,
            InteractionEventType eventType,
            String entityType,
            UUID entityId,
            UUID recommendationRequestId,
            Integer position,
            Instant occurredAt) {

        this.id = id;
        this.userId = userId;
        this.eventType = eventType;
        this.entityType = entityType;
        this.entityId = entityId;
        this.recommendationRequestId = recommendationRequestId;
        this.position = position;
        this.occurredAt = occurredAt;
    }
}