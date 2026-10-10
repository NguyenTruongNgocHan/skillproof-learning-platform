-- Organization governance and submission snapshots.
CREATE TABLE organization (
    id UUID PRIMARY KEY,
    owner_user_id UUID NOT NULL REFERENCES user_account(id),
    legal_name VARCHAR(200) NOT NULL,
    display_name VARCHAR(200) NOT NULL,
    website VARCHAR(500),
    industry VARCHAR(120) NOT NULL,
    country VARCHAR(120) NOT NULL,
    registration_number VARCHAR(120),
    contact_name VARCHAR(150) NOT NULL,
    contact_email VARCHAR(320) NOT NULL,
    contact_phone VARCHAR(60),
    status VARCHAR(20) NOT NULL
        CHECK (status IN ('DRAFT', 'PENDING', 'APPROVED', 'REJECTED')),
    current_application_revision_id UUID,
    review_reason VARCHAR(1000),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT uq_organization_owner
        UNIQUE (owner_user_id)
);

CREATE TABLE organization_review (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organization(id),
    reviewer_user_id UUID NOT NULL REFERENCES user_account(id),
    decision VARCHAR(20) NOT NULL
        CHECK (decision IN ('APPROVED', 'REJECTED')),
    reason VARCHAR(1000),
    reviewed_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE organization_membership (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organization(id),
    user_id UUID NOT NULL REFERENCES user_account(id),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT uq_org_member
        UNIQUE (organization_id, user_id)
);

CREATE TABLE organization_authority_grant (
    id UUID PRIMARY KEY,
    membership_id UUID NOT NULL REFERENCES organization_membership(id),
    authority VARCHAR(40) NOT NULL
        CHECK (
            authority IN (
                'MANAGE_PROFILE',
                'MANAGE_MEMBERS',
                'MANAGE_CONTENT',
                'ISSUE_CERTIFICATES'
            )
        ),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    granted_by UUID NOT NULL REFERENCES user_account(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT uq_org_grant
        UNIQUE (membership_id, authority)
);

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

CREATE TABLE organization_application_revision (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organization(id),
    revision_no INTEGER NOT NULL,
    legal_name VARCHAR(200) NOT NULL,
    display_name VARCHAR(200) NOT NULL,
    website VARCHAR(500),
    industry VARCHAR(120) NOT NULL,
    country VARCHAR(120) NOT NULL,
    registration_number VARCHAR(120),
    contact_name VARCHAR(150) NOT NULL,
    contact_email VARCHAR(320) NOT NULL,
    contact_phone VARCHAR(60),
    document_media_ids TEXT,
    status VARCHAR(20) NOT NULL,
    reviewer_user_id UUID,
    review_reason VARCHAR(1000),
    submitted_at TIMESTAMP WITH TIME ZONE NOT NULL,
    reviewed_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT uq_organization_application_revision UNIQUE (organization_id, revision_no)
);
ALTER TABLE organization ADD CONSTRAINT fk_org_current_revision FOREIGN KEY(current_application_revision_id) REFERENCES organization_application_revision(id);
CREATE UNIQUE INDEX uq_organization_invitation_pending ON organization_invitation(organization_id,email) WHERE status = 'PENDING';
CREATE INDEX ix_organization_invitation_email ON organization_invitation(email,status);
CREATE INDEX ix_organization_revision ON organization_application_revision(organization_id, revision_no DESC);
