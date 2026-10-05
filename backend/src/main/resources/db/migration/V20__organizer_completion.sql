-- New snapshot fields remain null on legacy certificates; do not fabricate historical data.
ALTER TABLE certificate ADD COLUMN issuer_name VARCHAR(200);
ALTER TABLE certificate ADD COLUMN learner_email VARCHAR(320);
CREATE TABLE media_cleanup_task (
    id UUID PRIMARY KEY,
    storage_key UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    next_attempt_at TIMESTAMPTZ NOT NULL,
    attempts INTEGER NOT NULL DEFAULT 0
);
CREATE INDEX ix_media_cleanup_ready ON media_cleanup_task (next_attempt_at, created_at);
INSERT INTO organization_authority_grant (id, membership_id, authority, active, granted_by, created_at)
SELECT gen_random_uuid(), m.id, a.authority, true, o.owner_user_id, CURRENT_TIMESTAMP
FROM organization o
JOIN organization_membership m ON m.organization_id = o.id AND m.user_id = o.owner_user_id
CROSS JOIN (VALUES ('MANAGE_PROFILE'), ('MANAGE_MEMBERS'), ('MANAGE_CONTENT'), ('ISSUE_CERTIFICATES')) a(authority)
ON CONFLICT (membership_id, authority) DO UPDATE SET active = true;

-- Cloned versions share immutable storage bytes while owning separate metadata.
ALTER TABLE media_asset DROP CONSTRAINT IF EXISTS media_asset_storage_key_key;

ALTER TABLE media_asset ADD COLUMN application_attachment_active BOOLEAN NOT NULL DEFAULT TRUE;

-- Keep the newest legacy pending invitation and invalidate redundant links.
WITH redundant AS (
  SELECT id, ROW_NUMBER() OVER (PARTITION BY organization_id, email ORDER BY created_at DESC, id DESC) AS ordinal
  FROM organization_invitation WHERE status = 'PENDING'
)
UPDATE organization_invitation SET status = 'REVOKED'
WHERE id IN (SELECT id FROM redundant WHERE ordinal > 1);
CREATE UNIQUE INDEX uq_organization_invitation_pending
ON organization_invitation (organization_id, email) WHERE status = 'PENDING';

-- Organization/version relationships are stable; issuer names and learner email
-- are deliberately left null for old certificates rather than invented.
UPDATE certificate c
SET organization_id = COALESCE(c.organization_id, p.organization_id),
    learning_path_version_id = COALESCE(c.learning_path_version_id, p.learning_path_version_id)
FROM certification_program p WHERE p.id = c.certification_program_id;

-- Import only the current legacy pending application when no snapshot exists.
-- submitted_at is the import time; older submission versions are not reconstructed.
INSERT INTO organization_application_revision (
    id, organization_id, revision_no, legal_name, display_name, website,
    industry, country, registration_number, contact_name, contact_email,
    contact_phone, document_media_ids, status, submitted_at
)
SELECT gen_random_uuid(), o.id, 1, o.legal_name, o.display_name, o.website,
    o.industry, o.country, o.registration_number, o.contact_name, o.contact_email,
    o.contact_phone,
    (SELECT string_agg(m.id::text, ',' ORDER BY m.created_at, m.id)
     FROM media_asset m WHERE m.scope = 'ORGANIZATION' AND m.organization_id = o.id
       AND m.application_attachment_active),
    'PENDING', CURRENT_TIMESTAMP
FROM organization o
WHERE o.status = 'PENDING'
  AND NOT EXISTS (SELECT 1 FROM organization_application_revision r WHERE r.organization_id = o.id);
