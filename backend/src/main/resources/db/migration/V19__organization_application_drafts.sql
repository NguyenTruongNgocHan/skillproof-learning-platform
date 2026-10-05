ALTER TABLE organization
    DROP CONSTRAINT IF EXISTS organization_status_check;

ALTER TABLE organization
    ADD CONSTRAINT organization_status_check
    CHECK (status IN ('DRAFT', 'PENDING', 'APPROVED', 'REJECTED'));
