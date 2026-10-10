-- Fresh database baseline. This schema intentionally does not upgrade the old V1–V21 history.
CREATE TABLE user_account (
    id UUID PRIMARY KEY, email VARCHAR(320) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    display_name VARCHAR(100) NOT NULL CHECK (length(btrim(display_name)) BETWEEN 2 AND 100),
    role VARCHAR(32) NOT NULL CHECK (role IN ('LEARNER','ORGANIZER','ADMIN')),
    status VARCHAR(32) NOT NULL CHECK (status IN ('PENDING_VERIFICATION','ACTIVE','DISABLED','LOCKED')),
    email_verified_at TIMESTAMPTZ,
    failed_login_count INTEGER NOT NULL DEFAULT 0 CHECK (failed_login_count >= 0),
    locked_until TIMESTAMPTZ, last_login_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL, updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE email_verification_token
(
    id              UUID PRIMARY KEY,
    user_account_id UUID        NOT NULL,
    token_hash      VARCHAR(64) NOT NULL,
    expires_at      TIMESTAMPTZ NOT NULL,
    consumed_at     TIMESTAMPTZ,
    invalidated_at  TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_email_verification_user
        FOREIGN KEY (user_account_id)
            REFERENCES user_account (id)
            ON DELETE CASCADE,

    CONSTRAINT uq_email_verification_token_hash
        UNIQUE (token_hash),

    CONSTRAINT ck_email_verification_token_hash
        CHECK (token_hash ~ '^[0-9a-f]{64}$'),

    CONSTRAINT ck_email_verification_expiry
        CHECK (expires_at > created_at)
);

CREATE TABLE user_profile (
    user_account_id UUID PRIMARY KEY REFERENCES user_account(id) ON DELETE CASCADE,
    headline VARCHAR(160),
    bio VARCHAR(1000),
    avatar_url VARCHAR(500),
    locale VARCHAR(16) NOT NULL DEFAULT 'vi-VN',
    timezone VARCHAR(64) NOT NULL DEFAULT 'Asia/Ho_Chi_Minh',
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE auth_session (
    id UUID PRIMARY KEY,
    user_account_id UUID NOT NULL REFERENCES user_account(id) ON DELETE CASCADE,
    user_agent VARCHAR(500),
    ip_address VARCHAR(64),
    created_at TIMESTAMPTZ NOT NULL,
    last_seen_at TIMESTAMPTZ NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ
);

CREATE TABLE refresh_token (
    id UUID PRIMARY KEY,
    session_id UUID NOT NULL REFERENCES auth_session(id) ON DELETE CASCADE,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    family_id UUID NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    consumed_at TIMESTAMPTZ,
    revoked_at TIMESTAMPTZ,
    replaced_by_id UUID
);

CREATE TABLE oauth_account (
    id UUID PRIMARY KEY,
    user_account_id UUID NOT NULL REFERENCES user_account(id) ON DELETE CASCADE,
    provider VARCHAR(32) NOT NULL,
    provider_subject VARCHAR(255) NOT NULL,
    provider_email VARCHAR(320),
    created_at TIMESTAMPTZ NOT NULL,
    UNIQUE(provider, provider_subject)
);

CREATE TABLE security_audit_event (
    id UUID PRIMARY KEY,
    user_account_id UUID REFERENCES user_account(id) ON DELETE SET NULL,
    event_type VARCHAR(64) NOT NULL,
    outcome VARCHAR(16) NOT NULL,
    subject VARCHAR(320),
    ip_address VARCHAR(64),
    user_agent VARCHAR(500),
    details VARCHAR(1000),
    occurred_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE password_reset_token (
    id UUID PRIMARY KEY,
    user_account_id UUID NOT NULL REFERENCES user_account(id) ON DELETE CASCADE,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ NOT NULL,
    consumed_at TIMESTAMPTZ,
    invalidated_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX ix_auth_session_user ON auth_session(user_account_id);
CREATE INDEX ix_refresh_token_family ON refresh_token(family_id);
CREATE INDEX ix_refresh_token_session ON refresh_token(session_id);
CREATE UNIQUE INDEX uq_verification_active ON email_verification_token(user_account_id) WHERE consumed_at IS NULL AND invalidated_at IS NULL;
CREATE INDEX ix_reset_user ON password_reset_token(user_account_id, created_at DESC);
CREATE INDEX ix_audit_user_time ON security_audit_event(user_account_id, occurred_at DESC);
CREATE TABLE learner_preferences (
    user_id UUID PRIMARY KEY REFERENCES user_account(id) ON DELETE CASCADE,
    career_goal VARCHAR(100), target_role VARCHAR(100), skill_level VARCHAR(20),
    weekly_goal VARCHAR(20), learning_methods VARCHAR(500),
    personalization_enabled BOOLEAN NOT NULL DEFAULT TRUE, exploration_mode BOOLEAN NOT NULL DEFAULT TRUE,
    goal_text VARCHAR(1000), experience_level VARCHAR(30), discovery_updated_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ NOT NULL
);
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
