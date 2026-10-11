-- Fresh-database baseline only. See README.md.

CREATE TABLE media_storage_object (
    id uuid PRIMARY KEY,
    deleted boolean NOT NULL DEFAULT FALSE,
    created_at timestamptz NOT NULL
);

CREATE TABLE media_asset (
    id uuid PRIMARY KEY,
    owner_user_id uuid NOT NULL REFERENCES user_account (id),
    scope varchar(16) NOT NULL,
    organization_id uuid REFERENCES organization (id),
    resource_id uuid REFERENCES course_resource (id),
    library_resource_id uuid REFERENCES library_resource (id),
    submission_id uuid REFERENCES assignment_submission (id),
    original_name varchar(180) NOT NULL,
    storage_key uuid NOT NULL REFERENCES media_storage_object (id),
    mime_type varchar(60) NOT NULL,
    size_bytes bigint NOT NULL,
    sha256 varchar(64) NOT NULL CHECK (sha256 ~ '^[0-9a-f]{64}$'),
    application_attachment_active boolean NOT NULL DEFAULT TRUE,
    created_at timestamptz NOT NULL,
    course_id uuid REFERENCES course (id),
    CONSTRAINT media_scope_allowed CHECK (scope IN ('AVATAR', 'ORGANIZATION', 'ISSUER', 'BRAND', 'COURSE_COVER', 'RESOURCE', 'LIBRARY', 'SUBMISSION')),
    CONSTRAINT media_size_allowed CHECK (size_bytes BETWEEN 1 AND 524288000),
    CONSTRAINT media_target_allowed CHECK ((scope = 'AVATAR' AND organization_id IS NULL AND resource_id IS NULL AND library_resource_id IS NULL AND submission_id IS NULL) OR (scope IN ('ORGANIZATION', 'ISSUER', 'BRAND') AND organization_id IS NOT NULL AND resource_id IS NULL AND library_resource_id IS NULL AND submission_id IS NULL) OR (scope = 'COURSE_COVER' AND course_id IS NOT NULL AND organization_id IS NULL AND resource_id IS NULL AND library_resource_id IS NULL AND submission_id IS NULL) OR (scope = 'RESOURCE' AND resource_id IS NOT NULL AND organization_id IS NULL AND library_resource_id IS NULL AND submission_id IS NULL) OR (scope = 'LIBRARY' AND library_resource_id IS NOT NULL AND organization_id IS NULL AND resource_id IS NULL AND submission_id IS NULL) OR (scope = 'SUBMISSION' AND submission_id IS NOT NULL AND organization_id IS NULL AND resource_id IS NULL AND library_resource_id IS NULL)),
    CONSTRAINT media_course_target CHECK ((scope = 'COURSE_COVER') = (course_id IS NOT NULL))
);

CREATE TABLE media_cleanup_task (
    id uuid PRIMARY KEY,
    storage_key uuid NOT NULL REFERENCES media_storage_object (id),
    created_at timestamptz NOT NULL,
    next_attempt_at timestamptz NOT NULL,
    attempts integer NOT NULL DEFAULT 0 CHECK (attempts >= 0)
);

CREATE TABLE organization_evidence (
    id uuid NOT NULL,
    organization_id uuid NOT NULL,
    media_id uuid NOT NULL,
    kind varchar(40) NOT NULL,
    purpose varchar(20) NOT NULL,
    title varchar(180) NOT NULL,
    expires_at timestamptz,
    status varchar(20) NOT NULL,
    reason varchar(1000),
    reviewer_id uuid,
    created_at timestamptz NOT NULL,
    reviewed_at timestamptz,
    PRIMARY KEY (id),
    UNIQUE (media_id),
    FOREIGN KEY (organization_id) REFERENCES organization (id),
    FOREIGN KEY (media_id) REFERENCES media_asset (id)
);

CREATE TABLE issuer_application (
    id uuid NOT NULL,
    organization_id uuid NOT NULL,
    status varchar(20) NOT NULL,
    scope varchar(2000) NOT NULL,
    evidence_ids text NOT NULL,
    reason varchar(1000),
    reviewer_id uuid,
    created_at timestamptz NOT NULL,
    reviewed_at timestamptz,
    valid_until timestamptz,
    PRIMARY KEY (id),
    FOREIGN KEY (organization_id) REFERENCES organization (id),
    scope_mode varchar(24) NOT NULL DEFAULT 'ALL_COURSES' CHECK (scope_mode IN ('ALL_COURSES', 'SELECTED_VERSIONS')),
    course_version_ids text NOT NULL DEFAULT ''
);

CREATE TABLE direct_upload_ticket (
    id uuid NOT NULL,
    owner_id uuid NOT NULL,
    scope varchar(20) NOT NULL,
    target_id uuid,
    storage_key uuid NOT NULL,
    filename varchar(180) NOT NULL,
    expected_size bigint NOT NULL,
    expires_at timestamptz NOT NULL,
    asset_id uuid,
    PRIMARY KEY (id),
    FOREIGN KEY (storage_key) REFERENCES media_storage_object (id),
    multipart_id varchar(1024),
    assembled boolean NOT NULL DEFAULT FALSE,
    cancelled boolean NOT NULL DEFAULT FALSE
);

CREATE TABLE organization_brand (
    id uuid PRIMARY KEY REFERENCES organization (id),
    logo_media_id uuid REFERENCES media_asset (id),
    cover_media_id uuid REFERENCES media_asset (id),
    updated_at timestamptz NOT NULL
);

CREATE TABLE course_artwork (
    id uuid PRIMARY KEY REFERENCES course (id),
    logo_media_id uuid REFERENCES media_asset (id),
    cover_media_id uuid REFERENCES media_asset (id),
    updated_at timestamptz NOT NULL
);

CREATE INDEX ix_media_storage_key ON media_asset (storage_key);

CREATE INDEX ix_media_resource ON media_asset (resource_id, created_at);

CREATE INDEX ix_media_org ON media_asset (organization_id, created_at);

CREATE INDEX ix_media_library ON media_asset (library_resource_id, created_at);

CREATE INDEX ix_media_submission ON media_asset (submission_id, created_at);

CREATE INDEX ix_media_cleanup_ready ON media_cleanup_task (next_attempt_at, created_at);

CREATE INDEX ix_direct_multipart_expiry ON direct_upload_ticket (expires_at) WHERE multipart_id IS NOT NULL AND assembled = FALSE;
