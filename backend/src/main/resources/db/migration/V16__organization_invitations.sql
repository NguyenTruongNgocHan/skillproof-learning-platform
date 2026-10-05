CREATE TABLE organization_invitation (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organization(id),
    email VARCHAR(320) NOT NULL,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    status VARCHAR(16) NOT NULL CHECK (status IN ('PENDING', 'ACCEPTED', 'REVOKED', 'EXPIRED')),
    invited_by UUID NOT NULL REFERENCES user_account(id),
    accepted_by UUID REFERENCES user_account(id),
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    accepted_at TIMESTAMPTZ
);

CREATE INDEX ix_org_invitation_org_status
    ON organization_invitation(organization_id, status);
CREATE INDEX ix_org_invitation_email_status
    ON organization_invitation(email, status);
