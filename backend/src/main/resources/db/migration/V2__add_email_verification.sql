ALTER TABLE user_account
    ADD COLUMN display_name VARCHAR(100);

UPDATE user_account
SET display_name =
        CASE
            WHEN LENGTH(BTRIM(SPLIT_PART(email, '@', 1))) >= 2
                THEN SPLIT_PART(email, '@', 1)
            ELSE 'SkillProof User'
        END
WHERE display_name IS NULL;

ALTER TABLE user_account
    ALTER COLUMN display_name SET NOT NULL;

ALTER TABLE user_account
    ADD CONSTRAINT ck_user_account_display_name
        CHECK (
            LENGTH(BTRIM(display_name)) BETWEEN 2 AND 100
        );

ALTER TABLE user_account
    ADD COLUMN email_verified_at TIMESTAMPTZ;


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

CREATE INDEX idx_email_verification_user
    ON email_verification_token (user_account_id);

CREATE INDEX idx_email_verification_expiry
    ON email_verification_token (expires_at);

CREATE UNIQUE INDEX uq_email_verification_active_per_user
    ON email_verification_token (user_account_id)
    WHERE consumed_at IS NULL
      AND invalidated_at IS NULL;