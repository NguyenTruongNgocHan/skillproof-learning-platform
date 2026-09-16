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

CREATE INDEX idx_auth_session_user ON auth_session(user_account_id);

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

CREATE INDEX idx_refresh_token_session ON refresh_token(session_id);
CREATE INDEX idx_refresh_token_family ON refresh_token(family_id);

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

CREATE INDEX idx_audit_user_time ON security_audit_event(user_account_id, occurred_at DESC);
CREATE INDEX idx_audit_type_time ON security_audit_event(event_type, occurred_at DESC);

ALTER TABLE user_account
    ADD COLUMN failed_login_count INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN locked_until TIMESTAMPTZ,
    ADD COLUMN last_login_at TIMESTAMPTZ;

ALTER TABLE user_account DROP CONSTRAINT ck_user_account_status;
ALTER TABLE user_account ADD CONSTRAINT ck_user_account_status
    CHECK (status IN ('PENDING_VERIFICATION', 'ACTIVE', 'DISABLED', 'LOCKED'));
