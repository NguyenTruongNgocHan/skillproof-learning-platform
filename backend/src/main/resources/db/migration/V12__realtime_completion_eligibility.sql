-- V8: durable completion evidence, certification eligibility context
-- and realtime battle source-of-truth.

-- Give the pre-V8 one-row-per-version completion policy a stable logical
-- identifier without changing version ownership.
ALTER TABLE completion_policy
    ADD COLUMN IF NOT EXISTS id UUID;

UPDATE completion_policy
SET id = gen_random_uuid()
WHERE id IS NULL;

ALTER TABLE completion_policy
    ALTER COLUMN id SET DEFAULT gen_random_uuid();

ALTER TABLE completion_policy
    ALTER COLUMN id SET NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_completion_policy_id
    ON completion_policy(id);


CREATE TABLE IF NOT EXISTS completion_evaluation (
    id UUID PRIMARY KEY,
    enrollment_id UUID NOT NULL REFERENCES enrollment(id),
    learning_path_version_id UUID NOT NULL REFERENCES learning_path_version(id),
    status VARCHAR(24) NOT NULL
        CHECK (status IN ('COMPLETED', 'NOT_COMPLETED')),
    evidence_snapshot_json JSONB NOT NULL,
    evaluated_at TIMESTAMPTZ NOT NULL,
    UNIQUE (enrollment_id, evaluated_at)
);

CREATE INDEX IF NOT EXISTS idx_completion_eval_enrollment
    ON completion_evaluation(enrollment_id, evaluated_at DESC);


CREATE TABLE IF NOT EXISTS certification_program (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organization(id),
    learning_path_version_id UUID NOT NULL REFERENCES learning_path_version(id),
    completion_policy_id UUID NOT NULL REFERENCES completion_policy(id),
    name VARCHAR(180) NOT NULL,
    status VARCHAR(24) NOT NULL
        CHECK (status IN ('DRAFT', 'ACTIVE', 'RETIRED')),
    created_by_user_id UUID NOT NULL REFERENCES user_account(id),
    created_at TIMESTAMPTZ NOT NULL,
    UNIQUE (organization_id, learning_path_version_id, name)
);

CREATE INDEX IF NOT EXISTS idx_cert_program_version
    ON certification_program(learning_path_version_id, status);


CREATE TABLE IF NOT EXISTS certificate_eligibility (
    id UUID PRIMARY KEY,
    certification_program_id UUID NOT NULL REFERENCES certification_program(id),
    learner_user_id UUID NOT NULL REFERENCES user_account(id),
    enrollment_id UUID NOT NULL REFERENCES enrollment(id),
    completion_evaluation_id UUID NOT NULL REFERENCES completion_evaluation(id),
    status VARCHAR(24) NOT NULL
        CHECK (status IN ('ELIGIBLE', 'NOT_ELIGIBLE')),
    evidence_snapshot_json JSONB NOT NULL,
    evaluated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_eligibility_program_learner
    ON certificate_eligibility(
        certification_program_id,
        learner_user_id,
        evaluated_at DESC
    );


CREATE TABLE IF NOT EXISTS battle_rating (
    learner_user_id UUID NOT NULL REFERENCES user_account(id),
    skill_context VARCHAR(160) NOT NULL,
    rating_value NUMERIC(10, 2) NOT NULL DEFAULT 1000,
    battle_count INTEGER NOT NULL DEFAULT 0,
    updated_at TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (learner_user_id, skill_context)
);


CREATE TABLE IF NOT EXISTS battle_session (
    id UUID PRIMARY KEY,
    skill_context VARCHAR(160) NOT NULL,
    state VARCHAR(24) NOT NULL
        CHECK (
            state IN (
                'WAITING',
                'MATCHED',
                'READY',
                'COUNTDOWN',
                'RUNNING',
                'FINISHED'
            )
        ),
    sequence_no BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL,
    started_at TIMESTAMPTZ,
    finished_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0
);


CREATE TABLE IF NOT EXISTS battle_participant (
    id UUID PRIMARY KEY,
    battle_session_id UUID NOT NULL
        REFERENCES battle_session(id) ON DELETE CASCADE,
    learner_user_id UUID NOT NULL REFERENCES user_account(id),
    rating_snapshot NUMERIC(10, 2) NOT NULL,
    final_score NUMERIC(10, 2) NOT NULL DEFAULT 0,
    ready_at TIMESTAMPTZ,
    joined_at TIMESTAMPTZ NOT NULL,
    UNIQUE (battle_session_id, learner_user_id)
);


CREATE TABLE IF NOT EXISTS battle_question (
    id UUID PRIMARY KEY,
    battle_session_id UUID NOT NULL
        REFERENCES battle_session(id) ON DELETE CASCADE,
    question_version_id UUID NOT NULL REFERENCES question_version(id),
    position INTEGER NOT NULL CHECK (position > 0),
    points NUMERIC(10, 2) NOT NULL DEFAULT 1,
    opened_at TIMESTAMPTZ,
    closes_at TIMESTAMPTZ,
    UNIQUE (battle_session_id, position),
    UNIQUE (battle_session_id, question_version_id)
);


CREATE TABLE IF NOT EXISTS battle_answer_submission (
    id UUID PRIMARY KEY,
    battle_session_id UUID NOT NULL
        REFERENCES battle_session(id) ON DELETE CASCADE,
    participant_id UUID NOT NULL REFERENCES battle_participant(id),
    battle_question_id UUID NOT NULL REFERENCES battle_question(id),
    submission_key UUID NOT NULL,
    selected_option_id UUID REFERENCES question_option(id),
    submitted_at TIMESTAMPTZ NOT NULL,
    is_correct BOOLEAN NOT NULL,
    awarded_points NUMERIC(10, 2) NOT NULL,
    UNIQUE (battle_session_id, submission_key),
    UNIQUE (participant_id, battle_question_id)
);


CREATE TABLE IF NOT EXISTS battle_result (
    battle_session_id UUID PRIMARY KEY REFERENCES battle_session(id),
    result_status VARCHAR(16) NOT NULL
        CHECK (result_status IN ('VALID', 'VOID')),
    winner_participant_id UUID REFERENCES battle_participant(id),
    is_draw BOOLEAN NOT NULL,
    result_reason TEXT,
    finalized_at TIMESTAMPTZ NOT NULL
);


CREATE TABLE IF NOT EXISTS battle_rating_history (
    id UUID PRIMARY KEY,
    learner_user_id UUID NOT NULL REFERENCES user_account(id),
    skill_context VARCHAR(160) NOT NULL,
    battle_session_id UUID NOT NULL REFERENCES battle_session(id),
    old_rating NUMERIC(10, 2) NOT NULL,
    new_rating NUMERIC(10, 2) NOT NULL,
    delta NUMERIC(10, 2) NOT NULL,
    recorded_at TIMESTAMPTZ NOT NULL,
    UNIQUE (learner_user_id, battle_session_id)
);

CREATE INDEX IF NOT EXISTS idx_battle_history_user
    ON battle_participant(learner_user_id, joined_at DESC);

CREATE INDEX IF NOT EXISTS idx_battle_leaderboard
    ON battle_rating(skill_context, rating_value DESC);