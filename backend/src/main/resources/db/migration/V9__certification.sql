-- Optional official certification belongs to a verified organization and immutable course version.
CREATE TABLE certification_program (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organization(id),
    course_version_id UUID NOT NULL REFERENCES course_version(id),
    completion_policy_id UUID NOT NULL REFERENCES completion_policy(id),
    name VARCHAR(180) NOT NULL,
    status VARCHAR(24) NOT NULL
        CHECK (status IN ('DRAFT', 'ACTIVE', 'RETIRED')),
    created_by_user_id UUID NOT NULL REFERENCES user_account(id),
    created_at TIMESTAMPTZ NOT NULL,
    UNIQUE (organization_id, course_version_id, name),
    FOREIGN KEY(course_version_id,completion_policy_id) REFERENCES completion_policy(version_id,id)
);
CREATE TABLE certificate_eligibility (
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
CREATE TABLE certificate (
    id UUID PRIMARY KEY,
    certification_program_id UUID NOT NULL REFERENCES certification_program(id),
    eligibility_id UUID NOT NULL REFERENCES certificate_eligibility(id),
    learner_user_id UUID NOT NULL REFERENCES user_account(id),
    serial_number VARCHAR(80) NOT NULL UNIQUE,
    status VARCHAR(16) NOT NULL CHECK (status IN ('ISSUED', 'REVOKED')),
    issued_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ,
    revocation_reason VARCHAR(1000),
    organization_id UUID NOT NULL REFERENCES organization(id), program_name VARCHAR(200) NOT NULL,
    course_version_id UUID NOT NULL REFERENCES course_version(id), issuer_name VARCHAR(200) NOT NULL, learner_email VARCHAR(320) NOT NULL,
    CHECK((status='ISSUED' AND revoked_at IS NULL AND revocation_reason IS NULL) OR (status='REVOKED' AND revoked_at IS NOT NULL AND length(btrim(revocation_reason)) > 0)),
    UNIQUE (certification_program_id, learner_user_id)
);
CREATE UNIQUE INDEX uq_active_certification_version ON certification_program(course_version_id) WHERE status='ACTIVE';
CREATE INDEX ix_eligibility_learner ON certificate_eligibility(certification_program_id,learner_user_id,evaluated_at DESC);
CREATE INDEX ix_certificate_learner ON certificate(learner_user_id,issued_at DESC);
