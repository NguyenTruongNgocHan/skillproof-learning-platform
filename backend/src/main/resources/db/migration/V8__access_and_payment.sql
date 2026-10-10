CREATE TABLE access_grant (
    id UUID NOT NULL PRIMARY KEY,
    learner_id UUID NOT NULL,
    product_type VARCHAR(20) NOT NULL,
    product_id UUID NOT NULL,
    source VARCHAR(20) NOT NULL,
    reference_id UUID NOT NULL,
    granted_by UUID,
    created_at TIMESTAMPTZ NOT NULL,
    expires_at TIMESTAMPTZ,
    revoked_at TIMESTAMPTZ,
    FOREIGN KEY(learner_id) REFERENCES user_account(id),
    FOREIGN KEY(granted_by) REFERENCES user_account(id),
    CHECK(product_type IN ('COURSE','CERTIFICATION','RESOURCE')),
    CHECK(source IN ('ORGANIZATION','PAYMENT')),
    UNIQUE(reference_id),
    CHECK(expires_at IS NULL OR expires_at > created_at)
);
CREATE TABLE payment_order (
    id UUID NOT NULL PRIMARY KEY,
    learner_id UUID NOT NULL,
    product_type VARCHAR(20) NOT NULL,
    product_id UUID NOT NULL,
    idempotency_key VARCHAR(100) NOT NULL,
    amount_vnd BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    provider_transaction VARCHAR(32),
    created_at TIMESTAMPTZ NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    settled_at TIMESTAMPTZ,
    FOREIGN KEY(learner_id) REFERENCES user_account(id),
    CHECK(product_type IN ('COURSE','CERTIFICATION','RESOURCE')),
    CHECK(status IN ('PENDING','PAID','FAILED','CANCELLED')),
    CHECK(amount_vnd BETWEEN 5000 AND 1000000000),
    CHECK(expires_at > created_at),
    CHECK((status='PENDING' AND settled_at IS NULL) OR (status<>'PENDING' AND settled_at IS NOT NULL)),
    UNIQUE(learner_id,idempotency_key)
);

CREATE UNIQUE INDEX uq_payment_provider_transaction ON payment_order(provider_transaction) WHERE status='PAID';
CREATE INDEX ix_access_product ON access_grant(learner_id,product_type,product_id);
CREATE INDEX ix_order_learner ON payment_order(learner_id,created_at DESC);
