ALTER TABLE organization
    ADD COLUMN IF NOT EXISTS current_application_revision_id UUID;

CREATE TABLE IF NOT EXISTS organization_application_revision (
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

CREATE INDEX IF NOT EXISTS ix_organization_application_revision_org
    ON organization_application_revision (organization_id, revision_no DESC);
