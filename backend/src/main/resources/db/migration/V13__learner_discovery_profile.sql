-- Evolves the old mandatory IT-oriented learner_preferences model into a
-- progressive discovery/personalization profile.
--
-- V7 is intentionally preserved because it may already exist in deployed DBs.

ALTER TABLE learner_preferences
    ALTER COLUMN career_goal DROP NOT NULL,
    ALTER COLUMN target_role DROP NOT NULL,
    ALTER COLUMN skill_level DROP NOT NULL,
    ALTER COLUMN weekly_goal DROP NOT NULL,
    ALTER COLUMN learning_methods DROP NOT NULL;

ALTER TABLE learner_preferences
    DROP CONSTRAINT IF EXISTS learner_preferences_skill_level_check;

ALTER TABLE learner_preferences
    ADD COLUMN IF NOT EXISTS personalization_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS exploration_mode BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS goal_text VARCHAR(1000),
    ADD COLUMN IF NOT EXISTS experience_level VARCHAR(30),
    ADD COLUMN IF NOT EXISTS discovery_updated_at TIMESTAMP WITH TIME ZONE;

CREATE TABLE learner_interest (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL
        REFERENCES user_account(id) ON DELETE CASCADE,

    -- Human-readable until the shared taxonomy is populated.
    -- A later migration can attach skill_domain_id / skill_id without
    -- changing the Discovery Profile API.
    label VARCHAR(100) NOT NULL,

    source VARCHAR(30) NOT NULL
        CHECK (source IN ('USER_SELECTED', 'USER_CONFIRMED_AI')),

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT uq_learner_interest_user_label
        UNIQUE (user_id, label)
);

CREATE INDEX idx_learner_interest_user
    ON learner_interest(user_id);

CREATE TABLE recommendation_interaction_event (
    id UUID PRIMARY KEY,

    user_id UUID NOT NULL
        REFERENCES user_account(id) ON DELETE CASCADE,

    event_type VARCHAR(60) NOT NULL,

    entity_type VARCHAR(50) NOT NULL,
    entity_id UUID,

    recommendation_request_id UUID,
    position INTEGER,

    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT ck_recommendation_event_position
        CHECK (position IS NULL OR position >= 0)
);

CREATE INDEX idx_recommendation_event_user_time
    ON recommendation_interaction_event(user_id, occurred_at DESC);

CREATE INDEX idx_recommendation_event_type_time
    ON recommendation_interaction_event(event_type, occurred_at DESC);

CREATE INDEX idx_recommendation_event_request
    ON recommendation_interaction_event(recommendation_request_id)
    WHERE recommendation_request_id IS NOT NULL;