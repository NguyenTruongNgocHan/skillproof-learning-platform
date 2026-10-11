-- Fresh-database baseline only. See README.md.

CREATE TABLE course (
    id uuid PRIMARY KEY,
    organization_id uuid REFERENCES organization (id),
    slug varchar(100) NOT NULL UNIQUE,
    title varchar(180) NOT NULL CHECK (length(btrim(title)) > 0),
    summary varchar(1000) NOT NULL CHECK (length(btrim(summary)) > 0),
    created_by uuid NOT NULL REFERENCES user_account (id),
    created_at timestamptz NOT NULL
);

CREATE TABLE course_version (
    id uuid PRIMARY KEY,
    course_id uuid NOT NULL REFERENCES course (id),
    version_no integer NOT NULL CHECK (version_no > 0),
    status varchar(12) NOT NULL CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED')),
    description text NOT NULL DEFAULT '',
    published_at timestamptz,
    created_at timestamptz NOT NULL,
    access_mode varchar(16) NOT NULL DEFAULT 'PUBLIC' CHECK (access_mode IN ('PUBLIC', 'RESTRICTED')),
    price_vnd bigint NOT NULL DEFAULT 0 CHECK (price_vnd = 0 OR price_vnd BETWEEN 5000 AND 1000000000),
    certification_price_vnd bigint NOT NULL DEFAULT 0 CHECK (certification_price_vnd = 0 OR certification_price_vnd BETWEEN 5000 AND 1000000000),
    review_status varchar(16) NOT NULL DEFAULT 'DRAFT' CHECK (review_status IN ('DRAFT', 'SUBMITTED', 'APPROVED', 'REJECTED')),
    review_reason varchar(1000),
    published_title varchar(180) NOT NULL DEFAULT '',
    published_summary varchar(1000) NOT NULL DEFAULT '',
    UNIQUE (course_id, version_no),
    UNIQUE (id, course_id),
    CHECK ((status = 'DRAFT' AND published_at IS NULL) OR (status <> 'DRAFT' AND published_at IS NOT NULL)),
    CHECK (access_mode = 'PUBLIC' OR (price_vnd = 0 AND certification_price_vnd = 0))
);

CREATE TABLE course_module (
    id uuid PRIMARY KEY,
    version_id uuid NOT NULL REFERENCES course_version (id),
    position integer NOT NULL CHECK (position > 0),
    title varchar(180) NOT NULL CHECK (length(pg_catalog.btrim(title)) > 0),
    UNIQUE (version_id, position),
    UNIQUE (id, version_id)
);

CREATE TABLE course_lesson (
    id uuid PRIMARY KEY,
    module_id uuid NOT NULL REFERENCES course_module (id),
    position integer NOT NULL CHECK (position > 0),
    title varchar(180) NOT NULL CHECK (length(btrim(title)) > 0),
    body text NOT NULL,
    UNIQUE (module_id, position),
    UNIQUE (id, module_id)
);

CREATE TABLE course_resource (
    id uuid PRIMARY KEY,
    module_id uuid NOT NULL REFERENCES course_module (id),
    lesson_id uuid NOT NULL,
    position integer NOT NULL CHECK (position > 0),
    kind varchar(12) NOT NULL,
    title varchar(180) NOT NULL CHECK (length(btrim(title)) > 0),
    body text,
    url varchar(1000),
    required_for_completion boolean NOT NULL DEFAULT TRUE,
    preview boolean NOT NULL DEFAULT FALSE,
    FOREIGN KEY (lesson_id, module_id) REFERENCES course_lesson (id, module_id),
    UNIQUE (lesson_id, position),
    CONSTRAINT resource_payload_allowed CHECK ((kind = 'ARTICLE' AND body IS NOT NULL AND length(btrim(body)) > 0 AND url IS NULL) OR (kind = 'LINK' AND body IS NULL AND url LIKE 'https://%') OR (kind = 'VIDEO' AND body IS NULL AND (url IS NULL OR url LIKE 'https://%')) OR (kind IN ('FILE', 'AUDIO', 'IMAGE') AND body IS NULL AND url IS NULL))
);

CREATE TABLE completion_policy (
    version_id uuid PRIMARY KEY REFERENCES course_version (id),
    id uuid NOT NULL UNIQUE,
    require_all_resources boolean NOT NULL DEFAULT TRUE,
    require_official_assessments boolean NOT NULL DEFAULT FALSE,
    UNIQUE (version_id, id)
);

CREATE TABLE enrollment (
    id uuid PRIMARY KEY,
    learner_id uuid NOT NULL REFERENCES user_account (id),
    course_id uuid NOT NULL REFERENCES course (id),
    version_id uuid NOT NULL,
    status varchar(12) NOT NULL CHECK (status IN ('ACTIVE', 'COMPLETED')),
    enrolled_at timestamptz NOT NULL,
    completed_at timestamptz,
    FOREIGN KEY (version_id, course_id) REFERENCES course_version (id, course_id),
    UNIQUE (learner_id, course_id),
    UNIQUE (id, version_id),
    CHECK ((status = 'ACTIVE' AND completed_at IS NULL) OR (status = 'COMPLETED' AND completed_at IS NOT NULL)),
    retained_free_access boolean NOT NULL DEFAULT FALSE
);

CREATE TABLE resource_progress (
    enrollment_id uuid NOT NULL REFERENCES enrollment (id),
    resource_id uuid NOT NULL REFERENCES course_resource (id),
    completed_at timestamptz NOT NULL,
    PRIMARY KEY (enrollment_id, resource_id)
);

CREATE TABLE lesson_progress (
    id uuid PRIMARY KEY,
    enrollment_id uuid NOT NULL REFERENCES enrollment (id),
    lesson_id uuid NOT NULL REFERENCES course_lesson (id),
    completed_at timestamptz NOT NULL,
    UNIQUE (enrollment_id, lesson_id)
);

CREATE TABLE completion_evaluation (
    id uuid PRIMARY KEY,
    enrollment_id uuid NOT NULL REFERENCES enrollment (id),
    course_version_id uuid NOT NULL REFERENCES course_version (id),
    status varchar(24) NOT NULL CHECK (status IN ('COMPLETED', 'NOT_COMPLETED')),
    evidence_snapshot_json jsonb NOT NULL,
    evaluated_at timestamptz NOT NULL
);

CREATE TABLE question_bank (
    id uuid PRIMARY KEY,
    organization_id uuid REFERENCES organization (id),
    title varchar(180) NOT NULL CHECK (length(pg_catalog.btrim(title)) > 0),
    created_by uuid NOT NULL REFERENCES user_account (id),
    created_at timestamptz NOT NULL,
    UNIQUE (id, organization_id)
);

CREATE TABLE question (
    id uuid PRIMARY KEY,
    bank_id uuid NOT NULL REFERENCES question_bank (id),
    created_at timestamptz NOT NULL
);

CREATE TABLE question_version (
    id uuid PRIMARY KEY,
    question_id uuid NOT NULL REFERENCES question (id),
    version_no integer NOT NULL CHECK (version_no > 0),
    stem varchar(2000) NOT NULL CHECK (length(pg_catalog.btrim(stem)) > 0),
    created_at timestamptz NOT NULL,
    UNIQUE (question_id, version_no),
    UNIQUE (id, question_id)
);

CREATE TABLE question_option (
    id uuid PRIMARY KEY,
    question_version_id uuid NOT NULL REFERENCES question_version (id),
    position integer NOT NULL CHECK (position > 0),
    body varchar(1000) NOT NULL CHECK (length(pg_catalog.btrim(body)) > 0),
    correct boolean NOT NULL DEFAULT FALSE,
    UNIQUE (question_version_id, position),
    UNIQUE (id, question_version_id)
);

CREATE TABLE assessment (
    id uuid PRIMARY KEY,
    organization_id uuid REFERENCES organization (id),
    version_id uuid NOT NULL REFERENCES course_version (id),
    kind varchar(12) NOT NULL CHECK (kind IN ('PRACTICE', 'MOCK', 'OFFICIAL')),
    status varchar(12) NOT NULL CHECK (status IN ('DRAFT', 'PUBLISHED')),
    title varchar(180) NOT NULL CHECK (length(pg_catalog.btrim(title)) > 0),
    duration_seconds integer NOT NULL CHECK (duration_seconds BETWEEN 60 AND 14400),
    pass_percent integer NOT NULL CHECK (pass_percent BETWEEN 1 AND 100),
    max_attempts integer NOT NULL CHECK (max_attempts BETWEEN 1 AND 20),
    owner_scope varchar(12) NOT NULL DEFAULT 'COURSE' CHECK (owner_scope IN ('COURSE', 'MODULE', 'LESSON')),
    owner_id uuid NOT NULL,
    required_for_completion boolean NOT NULL DEFAULT FALSE,
    created_at timestamptz NOT NULL,
    UNIQUE (id, version_id),
    UNIQUE (id, organization_id)
);

CREATE TABLE assessment_question (
    assessment_id uuid NOT NULL REFERENCES assessment (id),
    question_version_id uuid NOT NULL REFERENCES question_version (id),
    position integer NOT NULL CHECK (position > 0),
    points integer NOT NULL CHECK (points BETWEEN 1 AND 100),
    PRIMARY KEY (assessment_id, question_version_id),
    UNIQUE (assessment_id, position)
);

CREATE TABLE assessment_attempt (
    id uuid PRIMARY KEY,
    assessment_id uuid NOT NULL REFERENCES assessment (id),
    enrollment_id uuid NOT NULL REFERENCES enrollment (id),
    learner_id uuid NOT NULL REFERENCES user_account (id),
    status varchar(12) NOT NULL CHECK (status IN ('IN_PROGRESS', 'SUBMITTED', 'TIMED_OUT', 'SCORED')),
    started_at timestamptz NOT NULL,
    deadline_at timestamptz NOT NULL,
    submitted_at timestamptz,
    score_percent integer CHECK (score_percent BETWEEN 0 AND 100),
    passed boolean,
    CHECK (deadline_at > started_at),
    CONSTRAINT ck_attempt_terminal_score CHECK ((status IN ('SCORED', 'TIMED_OUT') AND score_percent IS NOT NULL AND passed IS NOT NULL) OR (status NOT IN ('SCORED', 'TIMED_OUT') AND score_percent IS NULL AND passed IS NULL))
);

CREATE TABLE attempt_question (
    attempt_id uuid NOT NULL REFERENCES assessment_attempt (id),
    question_version_id uuid NOT NULL REFERENCES question_version (id),
    position integer NOT NULL CHECK (position > 0),
    points integer NOT NULL CHECK (points BETWEEN 1 AND 100),
    selected_option_id uuid,
    PRIMARY KEY (attempt_id, question_version_id),
    UNIQUE (attempt_id, position),
    FOREIGN KEY (selected_option_id, question_version_id) REFERENCES question_option (id, question_version_id)
);

CREATE TABLE assessment_attempt_history (
    id uuid PRIMARY KEY,
    attempt_id uuid NOT NULL REFERENCES assessment_attempt (id),
    from_status varchar(12),
    to_status varchar(12) NOT NULL CHECK (to_status IN ('IN_PROGRESS', 'SUBMITTED', 'TIMED_OUT', 'SCORED')),
    occurred_at timestamptz NOT NULL
);

CREATE TABLE assignment (
    id uuid NOT NULL PRIMARY KEY,
    version_id uuid NOT NULL,
    owner_scope varchar(12) NOT NULL,
    owner_id uuid NOT NULL,
    title varchar(180) NOT NULL,
    instructions text NOT NULL,
    required boolean NOT NULL,
    pass_percent integer NOT NULL,
    max_submissions integer NOT NULL,
    due_at timestamptz,
    FOREIGN KEY (version_id) REFERENCES course_version (id),
    CHECK (owner_scope IN ('COURSE', 'MODULE', 'LESSON')),
    CHECK (pass_percent BETWEEN 1 AND 100 AND max_submissions BETWEEN 1 AND 20)
);

CREATE TABLE assignment_submission (
    id uuid NOT NULL PRIMARY KEY,
    assignment_id uuid NOT NULL,
    enrollment_id uuid NOT NULL,
    learner_id uuid NOT NULL,
    status varchar(16) NOT NULL,
    body text,
    link varchar(1000),
    created_at timestamptz NOT NULL,
    submitted_at timestamptz,
    score_percent integer,
    passed boolean,
    feedback varchar(2000),
    graded_by uuid,
    graded_at timestamptz,
    FOREIGN KEY (assignment_id) REFERENCES assignment (id),
    FOREIGN KEY (enrollment_id) REFERENCES enrollment (id),
    FOREIGN KEY (learner_id) REFERENCES user_account (id),
    FOREIGN KEY (graded_by) REFERENCES user_account (id),
    CHECK (status IN ('DRAFT', 'SUBMITTED', 'GRADED')),
    CHECK (score_percent IS NULL OR score_percent BETWEEN 0 AND 100),
    CHECK ((status = 'DRAFT' AND submitted_at IS NULL AND score_percent IS NULL AND passed IS NULL AND graded_at IS NULL AND graded_by IS NULL) OR (status = 'SUBMITTED' AND submitted_at IS NOT NULL AND score_percent IS NULL AND passed IS NULL AND graded_at IS NULL AND graded_by IS NULL) OR (status = 'GRADED' AND submitted_at IS NOT NULL AND score_percent IS NOT NULL AND passed IS NOT NULL AND graded_at IS NOT NULL AND graded_by IS NOT NULL))
);

CREATE TABLE library_resource (
    id uuid NOT NULL PRIMARY KEY,
    author_id uuid NOT NULL,
    organization_id uuid,
    title varchar(180) NOT NULL,
    summary varchar(1000) NOT NULL,
    body text NOT NULL,
    status varchar(16) NOT NULL,
    price_vnd bigint NOT NULL,
    access_mode varchar(16) NOT NULL DEFAULT 'PUBLIC' CHECK (access_mode IN ('PUBLIC', 'RESTRICTED')),
    created_at timestamptz NOT NULL,
    reviewed_by uuid,
    review_reason varchar(1000),
    FOREIGN KEY (author_id) REFERENCES user_account (id),
    FOREIGN KEY (organization_id) REFERENCES organization (id),
    FOREIGN KEY (reviewed_by) REFERENCES user_account (id),
    CHECK (status IN ('DRAFT', 'SUBMITTED', 'REJECTED', 'PUBLISHED', 'WITHDRAWN')),
    CHECK (price_vnd = 0 OR price_vnd BETWEEN 5000 AND 1000000000),
    CHECK (access_mode = 'PUBLIC' OR (organization_id IS NOT NULL AND price_vnd = 0))
);

CREATE TABLE learning_path (
    id uuid NOT NULL PRIMARY KEY,
    learner_id uuid NOT NULL,
    title varchar(180) NOT NULL,
    goal varchar(1000) NOT NULL,
    created_at timestamptz NOT NULL,
    FOREIGN KEY (learner_id) REFERENCES user_account (id)
);

CREATE TABLE learning_path_item (
    id uuid NOT NULL PRIMARY KEY,
    path_id uuid NOT NULL,
    course_version_id uuid NOT NULL,
    position integer NOT NULL,
    prerequisite_item_id uuid,
    FOREIGN KEY (path_id) REFERENCES learning_path (id),
    FOREIGN KEY (course_version_id) REFERENCES course_version (id),
    UNIQUE (id, path_id),
    FOREIGN KEY (prerequisite_item_id, path_id) REFERENCES learning_path_item (id, path_id),
    UNIQUE (path_id, position),
    UNIQUE (path_id, course_version_id),
    CHECK (position > 0)
);

CREATE TABLE assignment_rubric_criterion (
    id uuid PRIMARY KEY,
    assignment_id uuid NOT NULL REFERENCES assignment (id) ON DELETE CASCADE,
    title varchar(180) NOT NULL,
    weight integer NOT NULL CHECK (weight BETWEEN 1 AND 100),
    position integer NOT NULL CHECK (position > 0),
    UNIQUE (assignment_id, position)
);

CREATE TABLE submission_criterion_grade (
    id uuid PRIMARY KEY,
    submission_id uuid NOT NULL REFERENCES assignment_submission (id),
    criterion_id uuid NOT NULL REFERENCES assignment_rubric_criterion (id),
    score integer NOT NULL CHECK (score BETWEEN 0 AND 100),
    feedback varchar(1000) NOT NULL,
    UNIQUE (submission_id, criterion_id)
);

CREATE UNIQUE INDEX uq_course_draft ON course_version (course_id) WHERE status = 'DRAFT';

CREATE UNIQUE INDEX uq_course_published ON course_version (course_id) WHERE status = 'PUBLISHED';

CREATE INDEX ix_completion_enrollment ON completion_evaluation (enrollment_id, evaluated_at DESC);

CREATE UNIQUE INDEX uq_correct_option ON question_option (question_version_id) WHERE correct;

CREATE UNIQUE INDEX uq_active_attempt ON assessment_attempt (assessment_id, enrollment_id) WHERE status = 'IN_PROGRESS';

CREATE INDEX ix_attempt_learner ON assessment_attempt (learner_id, assessment_id, started_at DESC);

CREATE INDEX ix_attempt_deadline ON assessment_attempt (deadline_at) WHERE status = 'IN_PROGRESS';

CREATE INDEX ix_assessment_version ON assessment (version_id, status);

CREATE UNIQUE INDEX uq_assignment_pending ON assignment_submission (assignment_id, enrollment_id) WHERE status IN ('DRAFT', 'SUBMITTED');

CREATE INDEX ix_assignment_version ON assignment (version_id);

CREATE INDEX ix_assignment_submission ON assignment_submission (assignment_id, enrollment_id, created_at DESC);

CREATE INDEX ix_library_catalog ON library_resource (status, created_at DESC);

CREATE INDEX ix_path_owner ON learning_path (learner_id, created_at DESC);
