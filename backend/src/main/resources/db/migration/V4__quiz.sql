-- Question revisions and assessment/attempt snapshots.
CREATE TABLE question_bank (
    id UUID PRIMARY KEY,
    organization_id UUID REFERENCES organization(id),

    title VARCHAR(180) NOT NULL
        CHECK (length(trim(title)) > 0),

    created_by UUID NOT NULL REFERENCES user_account(id),
    created_at TIMESTAMPTZ NOT NULL,

    UNIQUE (id, organization_id)
);

CREATE TABLE question (
    id UUID PRIMARY KEY,
    bank_id UUID NOT NULL REFERENCES question_bank(id),
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE question_version (
    id UUID PRIMARY KEY,
    question_id UUID NOT NULL REFERENCES question(id),
    version_no INT NOT NULL CHECK (version_no > 0),

    stem VARCHAR(2000) NOT NULL
        CHECK (length(trim(stem)) > 0),

    created_at TIMESTAMPTZ NOT NULL,

    UNIQUE (question_id, version_no),
    UNIQUE (id, question_id)
);

CREATE TABLE question_option (
    id UUID PRIMARY KEY,
    question_version_id UUID NOT NULL REFERENCES question_version(id),
    position INT NOT NULL CHECK (position > 0),

    body VARCHAR(1000) NOT NULL
        CHECK (length(trim(body)) > 0),

    correct BOOLEAN NOT NULL DEFAULT FALSE,

    UNIQUE (question_version_id, position),
    UNIQUE (id, question_version_id)
);

CREATE TABLE assessment (
    id UUID PRIMARY KEY,
    organization_id UUID REFERENCES organization(id),
    version_id UUID NOT NULL REFERENCES course_version(id),

    kind VARCHAR(12) NOT NULL
        CHECK (kind IN ('PRACTICE', 'MOCK', 'OFFICIAL')),

    status VARCHAR(12) NOT NULL
        CHECK (status IN ('DRAFT', 'PUBLISHED')),

    title VARCHAR(180) NOT NULL
        CHECK (length(trim(title)) > 0),

    duration_seconds INT NOT NULL
        CHECK (duration_seconds BETWEEN 60 AND 14400),

    pass_percent INT NOT NULL
        CHECK (pass_percent BETWEEN 1 AND 100),

    max_attempts INT NOT NULL
        CHECK (max_attempts BETWEEN 1 AND 20),

    owner_scope VARCHAR(12) NOT NULL DEFAULT 'COURSE' CHECK(owner_scope IN ('COURSE','MODULE','LESSON')),
    owner_id UUID NOT NULL, required_for_completion BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL,

    UNIQUE (id, version_id),
    UNIQUE (id, organization_id)
);

CREATE TABLE assessment_question (
    assessment_id UUID NOT NULL REFERENCES assessment(id),
    question_version_id UUID NOT NULL REFERENCES question_version(id),

    position INT NOT NULL CHECK (position > 0),
    points INT NOT NULL CHECK (points BETWEEN 1 AND 100),

    PRIMARY KEY (assessment_id, question_version_id),
    UNIQUE (assessment_id, position)
);

CREATE TABLE assessment_attempt (
    id UUID PRIMARY KEY,
    assessment_id UUID NOT NULL REFERENCES assessment(id),
    enrollment_id UUID NOT NULL REFERENCES enrollment(id),
    learner_id UUID NOT NULL REFERENCES user_account(id),

    status VARCHAR(12) NOT NULL
        CHECK (
            status IN (
                'IN_PROGRESS',
                'SUBMITTED',
                'TIMED_OUT',
                'SCORED'
            )
        ),

    started_at TIMESTAMPTZ NOT NULL,
    deadline_at TIMESTAMPTZ NOT NULL,
    submitted_at TIMESTAMPTZ,

    score_percent INT
        CHECK (score_percent BETWEEN 0 AND 100),

    passed BOOLEAN,

    CHECK (deadline_at > started_at),

    CONSTRAINT ck_attempt_terminal_score CHECK (
        (
            status IN ('SCORED','TIMED_OUT')
            AND score_percent IS NOT NULL
            AND passed IS NOT NULL
        )
        OR
        (
            status NOT IN ('SCORED','TIMED_OUT')
            AND score_percent IS NULL
            AND passed IS NULL
        )
    )
);

CREATE TABLE attempt_question (
    attempt_id UUID NOT NULL REFERENCES assessment_attempt(id),
    question_version_id UUID NOT NULL REFERENCES question_version(id),

    position INT NOT NULL CHECK (position > 0),
    points INT NOT NULL CHECK (points BETWEEN 1 AND 100),

    selected_option_id UUID,

    PRIMARY KEY (attempt_id, question_version_id),
    UNIQUE (attempt_id, position),

    FOREIGN KEY (selected_option_id, question_version_id)
        REFERENCES question_option(id, question_version_id)
);

CREATE TABLE assessment_attempt_history (
    id UUID PRIMARY KEY,
    attempt_id UUID NOT NULL REFERENCES assessment_attempt(id),

    from_status VARCHAR(12),

    to_status VARCHAR(12) NOT NULL
        CHECK (
            to_status IN (
                'IN_PROGRESS',
                'SUBMITTED',
                'TIMED_OUT',
                'SCORED'
            )
        ),

    occurred_at TIMESTAMPTZ NOT NULL
);

CREATE UNIQUE INDEX uq_correct_option ON question_option(question_version_id) WHERE correct;
CREATE UNIQUE INDEX uq_active_attempt ON assessment_attempt(assessment_id,enrollment_id) WHERE status='IN_PROGRESS';
CREATE INDEX ix_attempt_learner ON assessment_attempt(learner_id,assessment_id,started_at DESC);
CREATE INDEX ix_attempt_deadline ON assessment_attempt(deadline_at) WHERE status='IN_PROGRESS';
CREATE INDEX ix_assessment_version ON assessment(version_id,status);
