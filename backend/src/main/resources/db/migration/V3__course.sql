-- Enrollments pin an immutable published Course version. Learning Path is a separate module.
CREATE TABLE course (
    id UUID PRIMARY KEY, organization_id UUID REFERENCES organization(id),
    slug VARCHAR(100) NOT NULL UNIQUE, title VARCHAR(180) NOT NULL CHECK (length(btrim(title)) > 0),
    summary VARCHAR(1000) NOT NULL CHECK (length(btrim(summary)) > 0),
    created_by UUID NOT NULL REFERENCES user_account(id), created_at TIMESTAMPTZ NOT NULL
);
CREATE TABLE course_version (
    id UUID PRIMARY KEY, course_id UUID NOT NULL REFERENCES course(id),
    version_no INTEGER NOT NULL CHECK(version_no > 0),
    status VARCHAR(12) NOT NULL CHECK(status IN ('DRAFT','PUBLISHED','ARCHIVED')),
    description TEXT NOT NULL DEFAULT '', published_at TIMESTAMPTZ, created_at TIMESTAMPTZ NOT NULL,
    access_mode VARCHAR(16) NOT NULL DEFAULT 'PUBLIC' CHECK(access_mode IN ('PUBLIC','RESTRICTED')),
    price_vnd BIGINT NOT NULL DEFAULT 0 CHECK(price_vnd = 0 OR price_vnd BETWEEN 5000 AND 1000000000),
    certification_price_vnd BIGINT NOT NULL DEFAULT 0 CHECK(certification_price_vnd = 0 OR certification_price_vnd BETWEEN 5000 AND 1000000000),
    review_status VARCHAR(16) NOT NULL DEFAULT 'DRAFT' CHECK(review_status IN ('DRAFT','SUBMITTED','APPROVED','REJECTED')),
    review_reason VARCHAR(1000), published_title VARCHAR(180) NOT NULL DEFAULT '', published_summary VARCHAR(1000) NOT NULL DEFAULT '',
    UNIQUE(course_id, version_no), UNIQUE(id,course_id),
    CHECK((status = 'DRAFT' AND published_at IS NULL) OR (status <> 'DRAFT' AND published_at IS NOT NULL)),
    CHECK(access_mode = 'PUBLIC' OR (price_vnd = 0 AND certification_price_vnd = 0))
);
CREATE UNIQUE INDEX uq_course_draft ON course_version(course_id) WHERE status = 'DRAFT';
CREATE UNIQUE INDEX uq_course_published ON course_version(course_id) WHERE status = 'PUBLISHED';
CREATE TABLE course_module (
    id UUID PRIMARY KEY,
    version_id UUID NOT NULL REFERENCES course_version(id),
    position INT NOT NULL CHECK (position > 0),

    title VARCHAR(180) NOT NULL
        CHECK (length(trim(title)) > 0),

    UNIQUE (version_id, position),
    UNIQUE (id, version_id)
);
CREATE TABLE course_lesson (
    id UUID PRIMARY KEY, module_id UUID NOT NULL REFERENCES course_module(id),
    position INTEGER NOT NULL CHECK(position > 0), title VARCHAR(180) NOT NULL CHECK(length(btrim(title)) > 0),
    body TEXT NOT NULL, UNIQUE(module_id,position), UNIQUE(id,module_id)
);
CREATE TABLE course_resource (
    id UUID PRIMARY KEY, module_id UUID NOT NULL REFERENCES course_module(id),
    lesson_id UUID NOT NULL, position INTEGER NOT NULL CHECK(position > 0),
    kind VARCHAR(12) NOT NULL CHECK(kind IN ('ARTICLE','LINK','VIDEO','FILE','AUDIO','IMAGE')),
    title VARCHAR(180) NOT NULL CHECK(length(btrim(title)) > 0), body TEXT, url VARCHAR(1000),
    required_for_completion BOOLEAN NOT NULL DEFAULT TRUE, preview BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY(lesson_id,module_id) REFERENCES course_lesson(id,module_id), UNIQUE(lesson_id,position),
    CHECK((kind='ARTICLE' AND body IS NOT NULL AND length(btrim(body)) > 0 AND url IS NULL)
       OR (kind IN ('LINK','VIDEO') AND body IS NULL AND url LIKE 'https://%')
       OR (kind IN ('FILE','AUDIO','IMAGE') AND body IS NULL AND url IS NULL))
);
CREATE TABLE completion_policy (
    version_id UUID PRIMARY KEY REFERENCES course_version(id),
    id UUID NOT NULL UNIQUE, require_all_resources BOOLEAN NOT NULL DEFAULT TRUE,
    require_official_assessments BOOLEAN NOT NULL DEFAULT FALSE, UNIQUE(version_id,id)
);
CREATE TABLE enrollment (
    id UUID PRIMARY KEY,
    learner_id UUID NOT NULL REFERENCES user_account(id),
    course_id UUID NOT NULL REFERENCES course(id),
    version_id UUID NOT NULL,

    status VARCHAR(12) NOT NULL
        CHECK (status IN ('ACTIVE', 'COMPLETED')),

    enrolled_at TIMESTAMPTZ NOT NULL,
    completed_at TIMESTAMPTZ,

    FOREIGN KEY (version_id, course_id)
        REFERENCES course_version(id, course_id),

    UNIQUE (learner_id, course_id),
    UNIQUE (id, version_id),

    CHECK (
        (status = 'ACTIVE' AND completed_at IS NULL)
        OR
        (status = 'COMPLETED' AND completed_at IS NOT NULL)
    )
);
CREATE TABLE resource_progress (
    enrollment_id UUID NOT NULL REFERENCES enrollment(id),
    resource_id UUID NOT NULL REFERENCES course_resource(id),
    completed_at TIMESTAMPTZ NOT NULL,

    PRIMARY KEY (enrollment_id, resource_id)
);
CREATE TABLE lesson_progress (
    id UUID PRIMARY KEY, enrollment_id UUID NOT NULL REFERENCES enrollment(id),
    lesson_id UUID NOT NULL REFERENCES course_lesson(id), completed_at TIMESTAMPTZ NOT NULL,
    UNIQUE(enrollment_id,lesson_id)
);
CREATE TABLE completion_evaluation (
    id UUID PRIMARY KEY,
    enrollment_id UUID NOT NULL REFERENCES enrollment(id),
    course_version_id UUID NOT NULL REFERENCES course_version(id),
    status VARCHAR(24) NOT NULL
        CHECK (status IN ('COMPLETED', 'NOT_COMPLETED')),
    evidence_snapshot_json JSONB NOT NULL,
    evaluated_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX ix_completion_enrollment ON completion_evaluation(enrollment_id,evaluated_at DESC);
