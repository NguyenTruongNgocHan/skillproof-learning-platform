-- Fresh-database baseline only. See README.md.

CREATE TABLE organization (
    id uuid PRIMARY KEY,
    owner_user_id uuid NOT NULL REFERENCES user_account (id),
    legal_name varchar(200) NOT NULL,
    display_name varchar(200) NOT NULL,
    website varchar(500),
    industry varchar(120) NOT NULL,
    country varchar(120) NOT NULL,
    registration_number varchar(120),
    contact_name varchar(150) NOT NULL,
    contact_email varchar(320) NOT NULL,
    contact_phone varchar(60),
    status varchar(20) NOT NULL,
    current_application_revision_id uuid,
    review_reason varchar(1000),
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone NOT NULL,
    CONSTRAINT uq_organization_owner UNIQUE (owner_user_id),
    CONSTRAINT organization_status_check CHECK (status IN ('DRAFT', 'PENDING', 'APPROVED', 'REJECTED', 'SUSPENDED', 'NEEDS_INFORMATION'))
);

CREATE TABLE organization_review (
    id uuid PRIMARY KEY,
    organization_id uuid NOT NULL REFERENCES organization (id),
    reviewer_user_id uuid NOT NULL REFERENCES user_account (id),
    decision varchar(20) NOT NULL,
    reason varchar(1000),
    reviewed_at timestamp with time zone NOT NULL,
    CONSTRAINT organization_review_decision_check CHECK (decision IN ('APPROVED', 'REJECTED', 'NEEDS_INFORMATION'))
);

CREATE TABLE organization_membership (
    id uuid PRIMARY KEY,
    organization_id uuid NOT NULL REFERENCES organization (id),
    user_id uuid NOT NULL REFERENCES user_account (id),
    active boolean NOT NULL DEFAULT TRUE,
    created_at timestamp with time zone NOT NULL,
    CONSTRAINT uq_org_member UNIQUE (organization_id, user_id)
);

CREATE TABLE organization_authority_grant (
    id uuid PRIMARY KEY,
    membership_id uuid NOT NULL REFERENCES organization_membership (id),
    authority varchar(40) NOT NULL,
    active boolean NOT NULL DEFAULT TRUE,
    granted_by uuid NOT NULL REFERENCES user_account (id),
    created_at timestamp with time zone NOT NULL,
    CONSTRAINT uq_org_grant UNIQUE (membership_id, authority),
    CONSTRAINT authority_allowed CHECK (authority IN ('MANAGE_PROFILE', 'MANAGE_MEMBERS', 'MANAGE_CONTENT', 'ISSUE_CERTIFICATES', 'VIEW_ANALYTICS', 'MANAGE_PRICING', 'MANAGE_FEEDBACK', 'GRADE_ASSIGNMENTS'))
);

CREATE TABLE organization_invitation (
    id uuid PRIMARY KEY,
    organization_id uuid NOT NULL REFERENCES organization (id),
    email varchar(320) NOT NULL,
    token_hash varchar(64) NOT NULL UNIQUE,
    status varchar(16) NOT NULL CHECK (status IN ('PENDING', 'ACCEPTED', 'REVOKED', 'EXPIRED')),
    invited_by uuid NOT NULL REFERENCES user_account (id),
    accepted_by uuid REFERENCES user_account (id),
    expires_at timestamptz NOT NULL,
    created_at timestamptz NOT NULL,
    accepted_at timestamptz
);

CREATE TABLE organization_application_revision (
    id uuid PRIMARY KEY,
    organization_id uuid NOT NULL REFERENCES organization (id),
    revision_no integer NOT NULL,
    legal_name varchar(200) NOT NULL,
    display_name varchar(200) NOT NULL,
    website varchar(500),
    industry varchar(120) NOT NULL,
    country varchar(120) NOT NULL,
    registration_number varchar(120),
    contact_name varchar(150) NOT NULL,
    contact_email varchar(320) NOT NULL,
    contact_phone varchar(60),
    document_media_ids text,
    status varchar(20) NOT NULL,
    reviewer_user_id uuid,
    review_reason varchar(1000),
    submitted_at timestamp with time zone NOT NULL,
    reviewed_at timestamp with time zone,
    CONSTRAINT uq_organization_application_revision UNIQUE (organization_id, revision_no),
    CONSTRAINT organization_application_revision_status_check CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'NEEDS_INFORMATION'))
);

ALTER TABLE organization ADD CONSTRAINT fk_org_current_revision FOREIGN KEY (current_application_revision_id) REFERENCES organization_application_revision (id);

CREATE UNIQUE INDEX uq_organization_invitation_pending ON organization_invitation (organization_id, email) WHERE status = 'PENDING';

CREATE INDEX ix_organization_invitation_email ON organization_invitation (email, status);

CREATE INDEX ix_organization_revision ON organization_application_revision (organization_id, revision_no DESC);
