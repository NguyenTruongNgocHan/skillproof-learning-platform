package com.skillproof.backend.recommendation.domain;

/**
 * Controlled interaction vocabulary used by Recommendation research.
 *
 * This enum must not become a generic UI click tracking catalog.
 * Events are introduced only when they have a defined research or
 * product recommendation purpose.
 */
public enum InteractionEventType {

    LEARNING_PATH_VIEWED,

    LEARNING_PATH_ENROLLED,

    RESOURCE_OPENED,

    RESOURCE_COMPLETED,

    QUIZ_COMPLETED,

    RECOMMENDATION_SHOWN,

    RECOMMENDATION_SELECTED,

    COMMUNITY_CONTENT_USED,

    BATTLE_COMPLETED
}