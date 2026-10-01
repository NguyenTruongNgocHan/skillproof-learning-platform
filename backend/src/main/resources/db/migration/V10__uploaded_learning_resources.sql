ALTER TABLE learning_resource
    DROP CONSTRAINT IF EXISTS learning_resource_kind_check;


ALTER TABLE learning_resource
    ADD CONSTRAINT learning_resource_kind_check
    CHECK (
        kind IN (
            'ARTICLE',
            'LINK',
            'VIDEO',
            'FILE',
            'AUDIO'
        )
    );


ALTER TABLE learning_resource
    DROP CONSTRAINT IF EXISTS learning_resource_check;


ALTER TABLE learning_resource
    ADD CONSTRAINT learning_resource_check
    CHECK (
        (
            kind = 'ARTICLE'
            AND body IS NOT NULL
            AND length(trim(body)) > 0
            AND url IS NULL
        )
        OR
        (
            kind IN ('LINK', 'VIDEO')
            AND url IS NOT NULL
            AND length(trim(url)) > 0
            AND body IS NULL
        )
        OR
        (
            kind IN ('FILE', 'AUDIO')
            AND body IS NULL
            AND url IS NULL
        )
    );


-- Multiple immutable versions can reference the same stored bytes;
-- no physical deletion while referenced.

ALTER TABLE media_asset
    DROP CONSTRAINT IF EXISTS media_asset_storage_key_key;


CREATE FUNCTION skillproof_guard_media_resource()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    IF NEW.scope = 'RESOURCE'
       AND NOT EXISTS (
            SELECT 1
            FROM learning_resource r
            JOIN learning_module m
                ON m.id = r.module_id
            JOIN learning_path_version v
                ON v.id = m.version_id
            WHERE r.id = NEW.resource_id
              AND v.status = 'DRAFT'
       )
    THEN
        RAISE EXCEPTION
            'Resource attachments require a draft version';
    END IF;

    RETURN NEW;
END
$$;


CREATE TRIGGER guard_media_resource
BEFORE INSERT
ON media_asset
FOR EACH ROW
EXECUTE FUNCTION skillproof_guard_media_resource();