-- Fresh-database baseline only. See README.md.

CREATE TABLE access_grant (
    id uuid NOT NULL PRIMARY KEY,
    learner_id uuid NOT NULL,
    product_type varchar(20) NOT NULL,
    product_id uuid NOT NULL,
    source varchar(20) NOT NULL,
    reference_id uuid NOT NULL,
    granted_by uuid,
    created_at timestamptz NOT NULL,
    expires_at timestamptz,
    revoked_at timestamptz,
    FOREIGN KEY (learner_id) REFERENCES user_account (id),
    FOREIGN KEY (granted_by) REFERENCES user_account (id),
    CHECK (product_type IN ('COURSE', 'CERTIFICATION', 'RESOURCE')),
    CHECK (source IN ('ORGANIZATION', 'PAYMENT')),
    UNIQUE (reference_id),
    CHECK (expires_at IS NULL OR expires_at > created_at),
    organization_id uuid REFERENCES organization (id),
    cohort varchar(120) NOT NULL DEFAULT ''
);

CREATE TABLE payment_order (
    id uuid NOT NULL PRIMARY KEY,
    learner_id uuid NOT NULL,
    product_type varchar(20) NOT NULL,
    product_id uuid NOT NULL,
    idempotency_key varchar(100) NOT NULL,
    amount_vnd bigint NOT NULL,
    status varchar(20) NOT NULL,
    provider_transaction varchar(32),
    created_at timestamptz NOT NULL,
    expires_at timestamptz NOT NULL,
    settled_at timestamptz,
    FOREIGN KEY (learner_id) REFERENCES user_account (id),
    CHECK (product_type IN ('COURSE', 'CERTIFICATION', 'RESOURCE')),
    CHECK (status IN ('PENDING', 'PAID', 'FAILED', 'CANCELLED')),
    CHECK (amount_vnd BETWEEN 5000 AND 1000000000),
    CHECK (expires_at > created_at),
    CHECK ((status = 'PENDING' AND settled_at IS NULL) OR (status <> 'PENDING' AND settled_at IS NOT NULL)),
    UNIQUE (learner_id, idempotency_key),
    organization_id uuid REFERENCES organization (id)
);

CREATE TABLE course_commercial (
    id uuid NOT NULL,
    organization_id uuid NOT NULL,
    state varchar(16) NOT NULL,
    access_mode varchar(16) NOT NULL,
    price_vnd bigint NOT NULL,
    certification_price_vnd bigint NOT NULL,
    updated_at timestamptz NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (id) REFERENCES course (id),
    CHECK (state IN ('ACTIVE', 'CLOSED', 'ARCHIVED')),
    CHECK (access_mode IN ('PUBLIC', 'RESTRICTED')),
    CHECK (price_vnd = 0 OR price_vnd BETWEEN 5000 AND 1000000000),
    CHECK (certification_price_vnd = 0 OR certification_price_vnd BETWEEN 5000 AND 1000000000),
    CHECK (access_mode = 'PUBLIC' OR (price_vnd = 0 AND certification_price_vnd = 0))
);

CREATE TABLE course_price_change (
    id uuid NOT NULL,
    course_id uuid NOT NULL,
    actor_id uuid NOT NULL,
    access_mode varchar(16) NOT NULL,
    previous_price bigint NOT NULL,
    price_vnd bigint NOT NULL,
    previous_certification_price bigint NOT NULL,
    certification_price_vnd bigint NOT NULL,
    reason varchar(1000) NOT NULL,
    created_at timestamptz NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (course_id) REFERENCES course (id)
);

CREATE TABLE course_promotion (
    id uuid NOT NULL,
    course_id uuid NOT NULL,
    name varchar(180) NOT NULL,
    percent integer NOT NULL,
    starts_at timestamptz NOT NULL,
    ends_at timestamptz NOT NULL,
    active boolean NOT NULL,
    created_by uuid NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (course_id) REFERENCES course (id),
    CHECK (percent BETWEEN 1 AND 90),
    CHECK (ends_at > starts_at)
);

CREATE UNIQUE INDEX uq_payment_provider_transaction ON payment_order (provider_transaction) WHERE status = 'PAID';

CREATE INDEX ix_access_product ON access_grant (learner_id, product_type, product_id);

CREATE INDEX ix_order_learner ON payment_order (learner_id, created_at DESC);

CREATE INDEX ix_access_grant_org ON access_grant (organization_id, created_at);

CREATE INDEX ix_payment_org_settled ON payment_order (organization_id, settled_at);

CREATE INDEX ix_access_org_cohort ON access_grant (organization_id, cohort, created_at DESC);
