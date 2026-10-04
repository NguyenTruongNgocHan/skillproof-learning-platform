DROP INDEX IF EXISTS uq_organization_membership_active_user;

ALTER TABLE learning_resource
    DROP CONSTRAINT IF EXISTS learning_resource_kind_check;

ALTER TABLE learning_resource
    ADD CONSTRAINT learning_resource_kind_check
    CHECK (kind IN ('ARTICLE', 'LINK', 'VIDEO', 'FILE', 'AUDIO'));
