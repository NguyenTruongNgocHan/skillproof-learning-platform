-- The database owns access metadata; file bytes live in a durable private volume.

CREATE TABLE media_asset (
    id UUID PRIMARY KEY,
    owner_user_id UUID NOT NULL REFERENCES user_account(id),

    scope VARCHAR(16) NOT NULL
        CHECK (
            scope IN (
                'AVATAR',
                'ORGANIZATION',
                'RESOURCE'
            )
        ),

    organization_id UUID REFERENCES organization(id),
    resource_id UUID REFERENCES learning_resource(id),

    original_name VARCHAR(180) NOT NULL,
    storage_key UUID NOT NULL UNIQUE,

    mime_type VARCHAR(60) NOT NULL,

    size_bytes BIGINT NOT NULL
        CHECK (size_bytes BETWEEN 1 AND 104857600),

    sha256 CHAR(64) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,

    CHECK (
        (
            scope = 'AVATAR'
            AND organization_id IS NULL
            AND resource_id IS NULL
        )
        OR
        (
            scope = 'ORGANIZATION'
            AND organization_id IS NOT NULL
            AND resource_id IS NULL
        )
        OR
        (
            scope = 'RESOURCE'
            AND organization_id IS NULL
            AND resource_id IS NOT NULL
        )
    )
);


CREATE INDEX ix_media_org
    ON media_asset (
        organization_id,
        created_at
    );


CREATE INDEX ix_media_resource
    ON media_asset (
        resource_id,
        created_at
    );


CREATE INDEX ix_media_owner_avatar
    ON media_asset (
        owner_user_id,
        created_at
    )
    WHERE scope = 'AVATAR';