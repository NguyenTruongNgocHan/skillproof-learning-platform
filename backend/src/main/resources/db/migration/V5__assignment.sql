CREATE TABLE assignment (
    id UUID NOT NULL PRIMARY KEY,
    version_id UUID NOT NULL,
    owner_scope VARCHAR(12) NOT NULL,
    owner_id UUID NOT NULL,
    title VARCHAR(180) NOT NULL,
    instructions TEXT NOT NULL,
    required BOOLEAN NOT NULL,
    pass_percent INTEGER NOT NULL,
    max_submissions INTEGER NOT NULL,
    due_at TIMESTAMPTZ,
    FOREIGN KEY(version_id) REFERENCES course_version(id),
    CHECK(owner_scope IN ('COURSE','MODULE','LESSON')),
    CHECK(pass_percent BETWEEN 1 AND 100 AND max_submissions BETWEEN 1 AND 20)
);
CREATE TABLE assignment_submission (
    id UUID NOT NULL PRIMARY KEY,
    assignment_id UUID NOT NULL,
    enrollment_id UUID NOT NULL,
    learner_id UUID NOT NULL,
    status VARCHAR(16) NOT NULL,
    body TEXT,
    link VARCHAR(1000),
    created_at TIMESTAMPTZ NOT NULL,
    submitted_at TIMESTAMPTZ,
    score_percent INTEGER,
    passed BOOLEAN,
    feedback VARCHAR(2000),
    graded_by UUID,
    graded_at TIMESTAMPTZ,
    FOREIGN KEY(assignment_id) REFERENCES assignment(id),
    FOREIGN KEY(enrollment_id) REFERENCES enrollment(id),
    FOREIGN KEY(learner_id) REFERENCES user_account(id),
    FOREIGN KEY(graded_by) REFERENCES user_account(id),
    CHECK(status IN ('DRAFT','SUBMITTED','GRADED')),
    CHECK(score_percent IS NULL OR score_percent BETWEEN 0 AND 100),
    CHECK((status='DRAFT' AND submitted_at IS NULL AND score_percent IS NULL AND passed IS NULL AND graded_at IS NULL AND graded_by IS NULL)
 OR (status='SUBMITTED' AND submitted_at IS NOT NULL AND score_percent IS NULL AND passed IS NULL AND graded_at IS NULL AND graded_by IS NULL)
 OR (status='GRADED' AND submitted_at IS NOT NULL AND score_percent IS NOT NULL AND passed IS NOT NULL AND graded_at IS NOT NULL AND graded_by IS NOT NULL))
);

CREATE UNIQUE INDEX uq_assignment_pending ON assignment_submission(assignment_id,enrollment_id) WHERE status IN ('DRAFT','SUBMITTED');
CREATE INDEX ix_assignment_version ON assignment(version_id);
CREATE INDEX ix_assignment_submission ON assignment_submission(assignment_id,enrollment_id,created_at DESC);
