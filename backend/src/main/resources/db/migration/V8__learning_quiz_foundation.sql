-- Published versions and used question versions retain their historical meaning.

CREATE TABLE learning_path (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organization(id),
    slug VARCHAR(100) NOT NULL,
    title VARCHAR(180) NOT NULL,
    summary VARCHAR(1000) NOT NULL,
    created_by UUID NOT NULL REFERENCES user_account(id),
    created_at TIMESTAMPTZ NOT NULL,

    UNIQUE (organization_id, slug),
    UNIQUE (id, organization_id),

    CHECK (length(trim(title)) > 0),
    CHECK (length(trim(summary)) > 0)
);


CREATE TABLE learning_path_version (
    id UUID PRIMARY KEY,
    path_id UUID NOT NULL REFERENCES learning_path(id),
    version_no INT NOT NULL CHECK (version_no > 0),

    status VARCHAR(12) NOT NULL
        CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED')),

    description TEXT NOT NULL DEFAULT '',
    published_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,

    UNIQUE (path_id, version_no),
    UNIQUE (id, path_id),

    CHECK (
        (status = 'DRAFT' AND published_at IS NULL)
        OR
        (status <> 'DRAFT' AND published_at IS NOT NULL)
    )
);


CREATE UNIQUE INDEX uq_path_current_version
    ON learning_path_version (path_id)
    WHERE status = 'PUBLISHED';


CREATE TABLE learning_module (
    id UUID PRIMARY KEY,
    version_id UUID NOT NULL REFERENCES learning_path_version(id),
    position INT NOT NULL CHECK (position > 0),

    title VARCHAR(180) NOT NULL
        CHECK (length(trim(title)) > 0),

    UNIQUE (version_id, position),
    UNIQUE (id, version_id)
);


CREATE TABLE learning_resource (
    id UUID PRIMARY KEY,
    module_id UUID NOT NULL REFERENCES learning_module(id),
    position INT NOT NULL CHECK (position > 0),

    kind VARCHAR(12) NOT NULL
        CHECK (kind IN ('ARTICLE', 'LINK', 'VIDEO')),

    title VARCHAR(180) NOT NULL
        CHECK (length(trim(title)) > 0),

    body TEXT,
    url VARCHAR(1000),

    UNIQUE (module_id, position),

    CHECK (
        (
            kind = 'ARTICLE'
            AND body IS NOT NULL
            AND length(trim(body)) > 0
            AND url IS NULL
        )
        OR
        (
            kind IN ('LINK', 'VIDEO')
            AND url IS NOT NULL
            AND length(trim(url)) > 0
            AND body IS NULL
        )
    )
);


CREATE TABLE completion_policy (
    version_id UUID PRIMARY KEY
        REFERENCES learning_path_version(id),

    require_all_resources BOOLEAN NOT NULL DEFAULT TRUE,
    require_official_assessments BOOLEAN NOT NULL DEFAULT TRUE
);


CREATE TABLE enrollment (
    id UUID PRIMARY KEY,
    learner_id UUID NOT NULL REFERENCES user_account(id),
    path_id UUID NOT NULL REFERENCES learning_path(id),
    version_id UUID NOT NULL,

    status VARCHAR(12) NOT NULL
        CHECK (status IN ('ACTIVE', 'COMPLETED')),

    enrolled_at TIMESTAMPTZ NOT NULL,
    completed_at TIMESTAMPTZ,

    FOREIGN KEY (version_id, path_id)
        REFERENCES learning_path_version(id, path_id),

    UNIQUE (learner_id, path_id),
    UNIQUE (id, version_id),

    CHECK (
        (status = 'ACTIVE' AND completed_at IS NULL)
        OR
        (status = 'COMPLETED' AND completed_at IS NOT NULL)
    )
);


CREATE TABLE resource_progress (
    enrollment_id UUID NOT NULL REFERENCES enrollment(id),
    resource_id UUID NOT NULL REFERENCES learning_resource(id),
    completed_at TIMESTAMPTZ NOT NULL,

    PRIMARY KEY (enrollment_id, resource_id)
);


CREATE TABLE question_bank (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organization(id),

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
    organization_id UUID NOT NULL REFERENCES organization(id),
    version_id UUID NOT NULL REFERENCES learning_path_version(id),

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

    CHECK (
        (
            status = 'SCORED'
            AND score_percent IS NOT NULL
            AND passed IS NOT NULL
        )
        OR
        (
            status <> 'SCORED'
            AND score_percent IS NULL
            AND passed IS NULL
        )
    )
);


CREATE INDEX ix_attempt_learner_assessment
    ON assessment_attempt (
        learner_id,
        assessment_id,
        started_at DESC
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


-- Defensive triggers: future code paths cannot mutate content
-- after publication or a question has been used.

CREATE FUNCTION skillproof_guard_version_content()
RETURNS trigger
LANGUAGE plpgsql
AS $$
DECLARE
    target_version UUID;
BEGIN
    IF TG_TABLE_NAME = 'learning_module' THEN
        target_version := COALESCE(NEW.version_id, OLD.version_id);
    ELSE
        SELECT version_id
        INTO target_version
        FROM learning_module
        WHERE id = COALESCE(NEW.module_id, OLD.module_id);
    END IF;

    IF EXISTS (
        SELECT 1
        FROM learning_path_version
        WHERE id = target_version
          AND status <> 'DRAFT'
    ) THEN
        RAISE EXCEPTION 'Published learning content is immutable';
    END IF;

    RETURN COALESCE(NEW, OLD);
END
$$;


CREATE TRIGGER guard_module
BEFORE INSERT OR UPDATE OR DELETE
ON learning_module
FOR EACH ROW
EXECUTE FUNCTION skillproof_guard_version_content();


CREATE TRIGGER guard_resource
BEFORE INSERT OR UPDATE OR DELETE
ON learning_resource
FOR EACH ROW
EXECUTE FUNCTION skillproof_guard_version_content();


CREATE FUNCTION skillproof_guard_used_question()
RETURNS trigger
LANGUAGE plpgsql
AS $$
DECLARE
    qv UUID;
BEGIN
    qv := CASE
        WHEN TG_TABLE_NAME = 'question_option'
            THEN COALESCE(NEW.question_version_id, OLD.question_version_id)
        ELSE COALESCE(NEW.id, OLD.id)
    END;

    IF EXISTS (
        SELECT 1
        FROM assessment_question
        WHERE question_version_id = qv
    )
    OR EXISTS (
        SELECT 1
        FROM attempt_question
        WHERE question_version_id = qv
    ) THEN
        RAISE EXCEPTION 'Used question version is immutable';
    END IF;

    RETURN COALESCE(NEW, OLD);
END
$$;


CREATE TRIGGER guard_question_version
BEFORE UPDATE OR DELETE
ON question_version
FOR EACH ROW
EXECUTE FUNCTION skillproof_guard_used_question();


CREATE TRIGGER guard_question_option
BEFORE INSERT OR UPDATE OR DELETE
ON question_option
FOR EACH ROW
EXECUTE FUNCTION skillproof_guard_used_question();


CREATE UNIQUE INDEX uq_correct_option_per_question_version
    ON question_option (question_version_id)
    WHERE correct;


CREATE FUNCTION skillproof_guard_progress_version()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM enrollment e
        JOIN learning_module m
            ON m.version_id = e.version_id
        JOIN learning_resource r
            ON r.module_id = m.id
        WHERE e.id = NEW.enrollment_id
          AND r.id = NEW.resource_id
    ) THEN
        RAISE EXCEPTION 'Resource is outside the enrolled version';
    END IF;

    RETURN NEW;
END
$$;


CREATE TRIGGER guard_progress_version
BEFORE INSERT OR UPDATE
ON resource_progress
FOR EACH ROW
EXECUTE FUNCTION skillproof_guard_progress_version();


CREATE FUNCTION skillproof_guard_policy()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM learning_path_version
        WHERE id = OLD.version_id
          AND status <> 'DRAFT'
    ) THEN
        RAISE EXCEPTION 'Published completion policy is immutable';
    END IF;

    RETURN COALESCE(NEW, OLD);
END
$$;


CREATE TRIGGER guard_completion_policy
BEFORE UPDATE OR DELETE
ON completion_policy
FOR EACH ROW
EXECUTE FUNCTION skillproof_guard_policy();


CREATE FUNCTION skillproof_guard_assessment()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    IF TG_TABLE_NAME = 'assessment' THEN
        IF OLD.status = 'PUBLISHED' THEN
            RAISE EXCEPTION 'Published assessment is immutable';
        END IF;
    ELSE
        IF EXISTS (
            SELECT 1
            FROM assessment
            WHERE id = COALESCE(
                NEW.assessment_id,
                OLD.assessment_id
            )
              AND status = 'PUBLISHED'
        ) THEN
            RAISE EXCEPTION
                'Published assessment questions are immutable';
        END IF;
    END IF;

    RETURN COALESCE(NEW, OLD);
END
$$;


CREATE TRIGGER guard_assessment
BEFORE UPDATE OR DELETE
ON assessment
FOR EACH ROW
EXECUTE FUNCTION skillproof_guard_assessment();


CREATE TRIGGER guard_assessment_questions
BEFORE INSERT OR UPDATE OR DELETE
ON assessment_question
FOR EACH ROW
EXECUTE FUNCTION skillproof_guard_assessment();


CREATE FUNCTION skillproof_guard_assessment_context()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM learning_path_version v
        JOIN learning_path p
            ON p.id = v.path_id
        WHERE v.id = NEW.version_id
          AND p.organization_id = NEW.organization_id
    ) THEN
        RAISE EXCEPTION
            'Assessment organization differs from path owner';
    END IF;

    RETURN NEW;
END
$$;


CREATE TRIGGER guard_assessment_context
BEFORE INSERT OR UPDATE
ON assessment
FOR EACH ROW
EXECUTE FUNCTION skillproof_guard_assessment_context();


CREATE FUNCTION skillproof_guard_assessment_question_context()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM assessment a
        JOIN question_version v
            ON v.id = NEW.question_version_id
        JOIN question q
            ON q.id = v.question_id
        JOIN question_bank b
            ON b.id = q.bank_id
        WHERE a.id = NEW.assessment_id
          AND a.organization_id = b.organization_id
    ) THEN
        RAISE EXCEPTION
            'Question belongs to another organization';
    END IF;

    RETURN NEW;
END
$$;


CREATE TRIGGER guard_assessment_question_context
BEFORE INSERT OR UPDATE
ON assessment_question
FOR EACH ROW
EXECUTE FUNCTION skillproof_guard_assessment_question_context();


CREATE FUNCTION skillproof_guard_attempt_context()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM assessment a
        JOIN enrollment e
            ON e.version_id = a.version_id
        WHERE a.id = NEW.assessment_id
          AND e.id = NEW.enrollment_id
          AND e.learner_id = NEW.learner_id
    ) THEN
        RAISE EXCEPTION
            'Attempt does not match enrollment, learner or version';
    END IF;

    RETURN NEW;
END
$$;


CREATE TRIGGER guard_attempt_context
BEFORE INSERT
ON assessment_attempt
FOR EACH ROW
EXECUTE FUNCTION skillproof_guard_attempt_context();


CREATE FUNCTION skillproof_guard_attempt_question_context()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM assessment_attempt t
        JOIN assessment_question aq
            ON aq.assessment_id = t.assessment_id
        WHERE t.id = NEW.attempt_id
          AND aq.question_version_id = NEW.question_version_id
    ) THEN
        RAISE EXCEPTION
            'Attempt question is outside assessment snapshot';
    END IF;

    RETURN NEW;
END
$$;


CREATE TRIGGER guard_attempt_question_context
BEFORE INSERT
ON attempt_question
FOR EACH ROW
EXECUTE FUNCTION skillproof_guard_attempt_question_context();


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


CREATE INDEX ix_attempt_history
    ON assessment_attempt_history (
        attempt_id,
        occurred_at
    );