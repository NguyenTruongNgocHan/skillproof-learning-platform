-- All file bytes are in S3. Metadata and durable cleanup tasks are in PostgreSQL.
CREATE TABLE media_storage_object (
    id UUID PRIMARY KEY, deleted BOOLEAN NOT NULL DEFAULT FALSE, created_at TIMESTAMPTZ NOT NULL
);
CREATE TABLE media_asset (
    id UUID PRIMARY KEY, owner_user_id UUID NOT NULL REFERENCES user_account(id),
    scope VARCHAR(16) NOT NULL CHECK(scope IN ('AVATAR','ORGANIZATION','RESOURCE','LIBRARY','SUBMISSION')),
    organization_id UUID REFERENCES organization(id), resource_id UUID REFERENCES course_resource(id),
    library_resource_id UUID REFERENCES library_resource(id), submission_id UUID REFERENCES assignment_submission(id),
    original_name VARCHAR(180) NOT NULL, storage_key UUID NOT NULL REFERENCES media_storage_object(id), mime_type VARCHAR(60) NOT NULL,
    size_bytes BIGINT NOT NULL CHECK(size_bytes BETWEEN 1 AND 104857600),
    sha256 VARCHAR(64) NOT NULL CHECK(sha256 ~ '^[0-9a-f]{64}$'),
    application_attachment_active BOOLEAN NOT NULL DEFAULT TRUE, created_at TIMESTAMPTZ NOT NULL,
    CHECK((scope='AVATAR' AND organization_id IS NULL AND resource_id IS NULL AND library_resource_id IS NULL AND submission_id IS NULL)
       OR (scope='ORGANIZATION' AND organization_id IS NOT NULL AND resource_id IS NULL AND library_resource_id IS NULL AND submission_id IS NULL)
       OR (scope='RESOURCE' AND resource_id IS NOT NULL AND organization_id IS NULL AND library_resource_id IS NULL AND submission_id IS NULL)
       OR (scope='LIBRARY' AND library_resource_id IS NOT NULL AND organization_id IS NULL AND resource_id IS NULL AND submission_id IS NULL)
       OR (scope='SUBMISSION' AND submission_id IS NOT NULL AND organization_id IS NULL AND resource_id IS NULL AND library_resource_id IS NULL))
);
CREATE INDEX ix_media_storage_key ON media_asset(storage_key);
CREATE INDEX ix_media_resource ON media_asset(resource_id,created_at);
CREATE INDEX ix_media_org ON media_asset(organization_id,created_at);
CREATE INDEX ix_media_library ON media_asset(library_resource_id,created_at);
CREATE INDEX ix_media_submission ON media_asset(submission_id,created_at);
CREATE TABLE media_cleanup_task (
    id UUID PRIMARY KEY, storage_key UUID NOT NULL REFERENCES media_storage_object(id), created_at TIMESTAMPTZ NOT NULL,
    next_attempt_at TIMESTAMPTZ NOT NULL, attempts INTEGER NOT NULL DEFAULT 0 CHECK(attempts >= 0)
);
CREATE INDEX ix_media_cleanup_ready ON media_cleanup_task(next_attempt_at,created_at);
