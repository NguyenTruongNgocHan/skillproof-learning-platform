CREATE TABLE library_resource (
    id UUID NOT NULL PRIMARY KEY,
    author_id UUID NOT NULL,
    organization_id UUID,
    title VARCHAR(180) NOT NULL,
    summary VARCHAR(1000) NOT NULL,
    body TEXT NOT NULL,
    status VARCHAR(16) NOT NULL,
    price_vnd BIGINT NOT NULL,
    access_mode VARCHAR(16) NOT NULL DEFAULT 'PUBLIC' CHECK(access_mode IN ('PUBLIC','RESTRICTED')),
    created_at TIMESTAMPTZ NOT NULL,
    reviewed_by UUID,
    review_reason VARCHAR(1000),
    FOREIGN KEY(author_id) REFERENCES user_account(id),
    FOREIGN KEY(organization_id) REFERENCES organization(id),
    FOREIGN KEY(reviewed_by) REFERENCES user_account(id),
    CHECK(status IN ('DRAFT','SUBMITTED','REJECTED','PUBLISHED','WITHDRAWN')),
    CHECK(price_vnd = 0 OR price_vnd BETWEEN 5000 AND 1000000000),
    CHECK(access_mode='PUBLIC' OR (organization_id IS NOT NULL AND price_vnd=0))
);
CREATE TABLE learning_path (
    id UUID NOT NULL PRIMARY KEY,
    learner_id UUID NOT NULL,
    title VARCHAR(180) NOT NULL,
    goal VARCHAR(1000) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    FOREIGN KEY(learner_id) REFERENCES user_account(id)
);
CREATE TABLE learning_path_item (
    id UUID NOT NULL PRIMARY KEY,
    path_id UUID NOT NULL,
    course_version_id UUID NOT NULL,
    position INTEGER NOT NULL,
    prerequisite_item_id UUID,
    FOREIGN KEY(path_id) REFERENCES learning_path(id),
    FOREIGN KEY(course_version_id) REFERENCES course_version(id),
    UNIQUE(id,path_id),
    FOREIGN KEY(prerequisite_item_id,path_id) REFERENCES learning_path_item(id,path_id),
    UNIQUE(path_id,position),
    UNIQUE(path_id,course_version_id),
    CHECK(position > 0)
);

CREATE INDEX ix_library_catalog ON library_resource(status,created_at DESC);
CREATE INDEX ix_path_owner ON learning_path(learner_id,created_at DESC);
