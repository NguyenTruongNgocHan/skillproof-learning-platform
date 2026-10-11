-- Fresh-database baseline only. See README.md.

CREATE TABLE certification_program (
    id uuid PRIMARY KEY,
    organization_id uuid NOT NULL REFERENCES organization (id),
    course_version_id uuid NOT NULL REFERENCES course_version (id),
    completion_policy_id uuid NOT NULL REFERENCES completion_policy (id),
    name varchar(180) NOT NULL,
    status varchar(24) NOT NULL CHECK (status IN ('DRAFT', 'ACTIVE', 'RETIRED')),
    created_by_user_id uuid NOT NULL REFERENCES user_account (id),
    created_at timestamptz NOT NULL,
    UNIQUE (organization_id, course_version_id, name),
    FOREIGN KEY (course_version_id, completion_policy_id) REFERENCES completion_policy (version_id, id)
);

CREATE TABLE certificate_eligibility (
    id uuid PRIMARY KEY,
    certification_program_id uuid NOT NULL REFERENCES certification_program (id),
    learner_user_id uuid NOT NULL REFERENCES user_account (id),
    enrollment_id uuid NOT NULL REFERENCES enrollment (id),
    completion_evaluation_id uuid NOT NULL REFERENCES completion_evaluation (id),
    status varchar(24) NOT NULL CHECK (status IN ('ELIGIBLE', 'NOT_ELIGIBLE')),
    evidence_snapshot_json jsonb NOT NULL,
    evaluated_at timestamptz NOT NULL
);

CREATE TABLE certificate (
    id uuid PRIMARY KEY,
    certification_program_id uuid NOT NULL REFERENCES certification_program (id),
    eligibility_id uuid NOT NULL REFERENCES certificate_eligibility (id),
    learner_user_id uuid NOT NULL REFERENCES user_account (id),
    serial_number varchar(80) NOT NULL UNIQUE,
    status varchar(16) NOT NULL CHECK (status IN ('ISSUED', 'REVOKED')),
    issued_at timestamptz NOT NULL,
    revoked_at timestamptz,
    revocation_reason varchar(1000),
    organization_id uuid NOT NULL REFERENCES organization (id),
    program_name varchar(200) NOT NULL,
    course_version_id uuid NOT NULL REFERENCES course_version (id),
    issuer_name varchar(200) NOT NULL,
    learner_email varchar(320) NOT NULL,
    CHECK ((status = 'ISSUED' AND revoked_at IS NULL AND revocation_reason IS NULL) OR (status = 'REVOKED' AND revoked_at IS NOT NULL AND length(btrim(revocation_reason)) > 0)),
    UNIQUE (certification_program_id, learner_user_id)
);

CREATE TABLE audit_entry (
    id uuid NOT NULL,
    actor_id uuid NOT NULL,
    organization_id uuid,
    action varchar(80) NOT NULL,
    target_id uuid,
    details text NOT NULL,
    created_at timestamptz NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE workspace_notice (
    id uuid NOT NULL,
    recipient_id uuid NOT NULL,
    organization_id uuid,
    category varchar(80) NOT NULL,
    message varchar(1000) NOT NULL,
    link varchar(500) NOT NULL,
    created_at timestamptz NOT NULL,
    read_at timestamptz,
    PRIMARY KEY (id)
);

CREATE TABLE experience_receipt (
    id uuid NOT NULL,
    actor_id uuid NOT NULL,
    type varchar(20) NOT NULL,
    target_id uuid NOT NULL,
    accessed_at timestamptz NOT NULL,
    PRIMARY KEY (id),
    UNIQUE (actor_id, type, target_id)
);

CREATE TABLE learner_review (
    id uuid NOT NULL,
    actor_id uuid NOT NULL,
    organization_id uuid,
    type varchar(20) NOT NULL,
    target_id uuid NOT NULL,
    rating integer NOT NULL,
    body varchar(4000) NOT NULL,
    status varchar(20) NOT NULL,
    reply varchar(2000),
    replied_by uuid,
    moderation_reason varchar(1000),
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL,
    PRIMARY KEY (id),
    UNIQUE (actor_id, type, target_id),
    CHECK (rating BETWEEN 1 AND 5),
    CHECK (status IN ('VISIBLE', 'HIDDEN'))
);

CREATE TABLE content_report (
    id uuid NOT NULL,
    actor_id uuid NOT NULL,
    review_id uuid NOT NULL,
    reason varchar(2000) NOT NULL,
    status varchar(20) NOT NULL,
    resolution varchar(2000),
    moderator_id uuid,
    created_at timestamptz NOT NULL,
    PRIMARY KEY (id),
    UNIQUE (actor_id, review_id),
    FOREIGN KEY (review_id) REFERENCES learner_review (id)
);

CREATE TABLE marketing_event (
    id uuid NOT NULL,
    organization_id uuid NOT NULL,
    version_id uuid,
    actor_id uuid,
    visitor_id uuid NOT NULL,
    kind varchar(30) NOT NULL,
    source varchar(120) NOT NULL,
    campaign varchar(120) NOT NULL,
    created_at timestamptz NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT marketing_event_kind_check CHECK (kind IN ('IMPRESSION', 'DETAIL_VIEW', 'ORG_VIEW', 'CHECKOUT', 'ENROLLMENT', 'LEARNING', 'COMPLETION', 'PAYMENT'))
);

CREATE TABLE certificate_provenance (
    id uuid PRIMARY KEY REFERENCES certificate (id),
    issuer_application_id uuid NOT NULL REFERENCES issuer_application (id),
    credential_hash varchar(64) NOT NULL,
    canonical_payload text NOT NULL,
    captured_at timestamptz NOT NULL
);

CREATE TABLE review_moderation_entry (
    id uuid PRIMARY KEY,
    review_id uuid NOT NULL REFERENCES learner_review (id),
    report_id uuid NOT NULL REFERENCES content_report (id),
    actor_id uuid NOT NULL REFERENCES user_account (id),
    decision varchar(20) NOT NULL CHECK (decision IN ('HIDDEN', 'RETAINED')),
    reason varchar(1000) NOT NULL,
    created_at timestamptz NOT NULL
);

CREATE UNIQUE INDEX uq_active_certification_version ON certification_program (course_version_id) WHERE status = 'ACTIVE';

CREATE INDEX ix_eligibility_learner ON certificate_eligibility (certification_program_id, learner_user_id, evaluated_at DESC);

CREATE INDEX ix_certificate_learner ON certificate (learner_user_id, issued_at DESC);

CREATE INDEX ix_marketing_org_time ON marketing_event (organization_id, created_at);

CREATE INDEX ix_feedback_org_time ON learner_review (organization_id, created_at);

CREATE INDEX ix_audit_org_time ON audit_entry (organization_id, created_at);

CREATE INDEX ix_notice_recipient_time ON workspace_notice (recipient_id, created_at);

CREATE INDEX ix_review_moderation_history ON review_moderation_entry (review_id, created_at DESC);
