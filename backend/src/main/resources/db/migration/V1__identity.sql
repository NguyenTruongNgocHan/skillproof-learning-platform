-- Fresh-database baseline only. See README.md.

CREATE TABLE user_account (
    id uuid PRIMARY KEY,
    email varchar(320) NOT NULL UNIQUE,
    password_hash varchar(100) NOT NULL,
    display_name varchar(100) NOT NULL CHECK (length(btrim(display_name)) BETWEEN 2 AND 100),
    role varchar(32) NOT NULL CHECK (role IN ('LEARNER', 'ORGANIZER', 'ADMIN')),
    status varchar(32) NOT NULL CHECK (status IN ('PENDING_VERIFICATION', 'ACTIVE', 'DISABLED', 'LOCKED')),
    email_verified_at timestamptz,
    failed_login_count integer NOT NULL DEFAULT 0 CHECK (failed_login_count >= 0),
    locked_until timestamptz,
    last_login_at timestamptz,
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL
);

CREATE TABLE email_verification_token (
    id uuid PRIMARY KEY,
    user_account_id uuid NOT NULL,
    token_hash varchar(64) NOT NULL,
    expires_at timestamptz NOT NULL,
    consumed_at timestamptz,
    invalidated_at timestamptz,
    created_at timestamptz NOT NULL,
    CONSTRAINT fk_email_verification_user FOREIGN KEY (user_account_id) REFERENCES user_account (id) ON DELETE CASCADE,
    CONSTRAINT uq_email_verification_token_hash UNIQUE (token_hash),
    CONSTRAINT ck_email_verification_token_hash CHECK (token_hash ~ '^[0-9a-f]{64}$'),
    CONSTRAINT ck_email_verification_expiry CHECK (expires_at > created_at)
);

CREATE TABLE user_profile (
    user_account_id uuid PRIMARY KEY REFERENCES user_account (id) ON DELETE CASCADE,
    headline varchar(160),
    bio varchar(1000),
    avatar_url varchar(500),
    locale varchar(16) NOT NULL DEFAULT 'vi-VN',
    timezone varchar(64) NOT NULL DEFAULT 'Asia/Ho_Chi_Minh',
    updated_at timestamptz NOT NULL
);

CREATE TABLE auth_session (
    id uuid PRIMARY KEY,
    user_account_id uuid NOT NULL REFERENCES user_account (id) ON DELETE CASCADE,
    user_agent varchar(500),
    ip_address varchar(64),
    created_at timestamptz NOT NULL,
    last_seen_at timestamptz NOT NULL,
    expires_at timestamptz NOT NULL,
    revoked_at timestamptz
);

CREATE TABLE refresh_token (
    id uuid PRIMARY KEY,
    session_id uuid NOT NULL REFERENCES auth_session (id) ON DELETE CASCADE,
    token_hash varchar(64) NOT NULL UNIQUE,
    family_id uuid NOT NULL,
    expires_at timestamptz NOT NULL,
    created_at timestamptz NOT NULL,
    consumed_at timestamptz,
    revoked_at timestamptz,
    replaced_by_id uuid
);

CREATE TABLE oauth_account (
    id uuid PRIMARY KEY,
    user_account_id uuid NOT NULL REFERENCES user_account (id) ON DELETE CASCADE,
    provider varchar(32) NOT NULL,
    provider_subject varchar(255) NOT NULL,
    provider_email varchar(320),
    created_at timestamptz NOT NULL,
    UNIQUE (provider, provider_subject)
);

CREATE TABLE security_audit_event (
    id uuid PRIMARY KEY,
    user_account_id uuid REFERENCES user_account (id) ON DELETE SET NULL,
    event_type varchar(64) NOT NULL,
    outcome varchar(16) NOT NULL,
    subject varchar(320),
    ip_address varchar(64),
    user_agent varchar(500),
    details varchar(1000),
    occurred_at timestamptz NOT NULL
);

CREATE TABLE password_reset_token (
    id uuid PRIMARY KEY,
    user_account_id uuid NOT NULL REFERENCES user_account (id) ON DELETE CASCADE,
    token_hash varchar(64) NOT NULL UNIQUE,
    expires_at timestamptz NOT NULL,
    consumed_at timestamptz,
    invalidated_at timestamptz,
    created_at timestamptz NOT NULL
);

CREATE TABLE learner_preferences (
    user_id uuid PRIMARY KEY REFERENCES user_account (id) ON DELETE CASCADE,
    career_goal varchar(100),
    target_role varchar(100),
    skill_level varchar(20),
    weekly_goal varchar(20),
    learning_methods varchar(500),
    personalization_enabled boolean NOT NULL DEFAULT TRUE,
    exploration_mode boolean NOT NULL DEFAULT TRUE,
    goal_text varchar(1000),
    experience_level varchar(30),
    discovery_updated_at timestamptz,
    updated_at timestamptz NOT NULL
);

CREATE TABLE learner_interest (
    id uuid PRIMARY KEY,
    user_id uuid NOT NULL REFERENCES user_account (id) ON DELETE CASCADE,
    label varchar(100) NOT NULL,
    source varchar(30) NOT NULL CHECK (source IN ('USER_SELECTED', 'USER_CONFIRMED_AI')),
    created_at timestamp with time zone NOT NULL,
    CONSTRAINT uq_learner_interest_user_label UNIQUE (user_id, label)
);

CREATE INDEX ix_auth_session_user ON auth_session (user_account_id);

CREATE INDEX ix_refresh_token_family ON refresh_token (family_id);

CREATE INDEX ix_refresh_token_session ON refresh_token (session_id);

CREATE UNIQUE INDEX uq_verification_active ON email_verification_token (user_account_id) WHERE consumed_at IS NULL AND invalidated_at IS NULL;

CREATE INDEX ix_reset_user ON password_reset_token (user_account_id, created_at DESC);

CREATE INDEX ix_audit_user_time ON security_audit_event (user_account_id, occurred_at DESC);
