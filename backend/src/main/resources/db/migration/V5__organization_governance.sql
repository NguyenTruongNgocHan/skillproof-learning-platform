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
        CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
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


CREATE INDEX ix_org_review_org
    ON organization_review (organization_id, reviewed_at);


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