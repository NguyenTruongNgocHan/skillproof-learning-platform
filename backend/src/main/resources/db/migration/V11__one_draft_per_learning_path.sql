-- A concurrent request cannot create a second draft of the same path.
-- Published versions remain separately constrained by uq_path_current_version.
CREATE UNIQUE INDEX uq_learning_path_single_draft
    ON learning_path_version(path_id)
    WHERE status = 'DRAFT';
