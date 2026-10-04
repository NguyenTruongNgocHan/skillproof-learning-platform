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
    UNIQUE (certification_program_id, learner_user_id)
);

CREATE INDEX ix_certificate_serial ON certificate(serial_number);
