ALTER TABLE certificate
    ADD COLUMN IF NOT EXISTS organization_id UUID,
    ADD COLUMN IF NOT EXISTS program_name VARCHAR(200),
    ADD COLUMN IF NOT EXISTS learning_path_version_id UUID;
