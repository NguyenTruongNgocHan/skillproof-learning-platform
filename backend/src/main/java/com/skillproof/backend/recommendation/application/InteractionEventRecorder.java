package com.skillproof.backend.recommendation.application;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillproof.backend.recommendation.domain.InteractionEventType;
import com.skillproof.backend.recommendation.infrastructure.InteractionEventEntity;
import com.skillproof.backend.recommendation.infrastructure.InteractionEventRepository;

@Service
public class InteractionEventRecorder {

    private final InteractionEventRepository events;

    public InteractionEventRecorder(InteractionEventRepository events) {
        this.events = events;
    }

    @Transactional
    public void record(
            UUID userId,
            InteractionEventType eventType,
            String entityType,
            UUID entityId) {

        record(
                userId,
                eventType,
                entityType,
                entityId,
                null,
                null);
    }

    @Transactional
    public void recordRecommendation(
            UUID userId,
            InteractionEventType eventType,
            String entityType,
            UUID entityId,
            UUID recommendationRequestId,
            Integer position) {

        if (eventType != InteractionEventType.RECOMMENDATION_SHOWN
                && eventType != InteractionEventType.RECOMMENDATION_SELECTED) {
            throw new IllegalArgumentException(
                    "Recommendation context is only valid for recommendation events");
        }

        record(
                userId,
                eventType,
                entityType,
                entityId,
                recommendationRequestId,
                position);
    }

    private void record(
            UUID userId,
            InteractionEventType eventType,
            String entityType,
            UUID entityId,
            UUID recommendationRequestId,
            Integer position) {

        if (userId == null || eventType == null) {
            throw new IllegalArgumentException(
                    "Interaction event requires a user and event type");
        }

        String normalizedEntityType = normalizeEntityType(entityType);

        if (position != null && position < 0) {
            throw new IllegalArgumentException(
                    "Recommendation position cannot be negative");
        }

        events.save(
                new InteractionEventEntity(
                        UUID.randomUUID(),
                        userId,
                        eventType,
                        normalizedEntityType,
                        entityId,
                        recommendationRequestId,
                        position,
                        Instant.now()));
    }

    private String normalizeEntityType(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "Interaction event requires an entity type");
        }

        String normalized = value.trim().toUpperCase();

        if (normalized.length() > 50) {
            throw new IllegalArgumentException(
                    "Interaction entity type is too long");
        }

        return normalized;
    }
}