-- Additive freeze migration. Never change V1--V7 on an existing Flyway database.
ALTER TABLE course ADD COLUMN suspended boolean NOT NULL DEFAULT false;
ALTER TABLE course ADD COLUMN moderation_reason varchar(1000);
ALTER TABLE question_bank ADD COLUMN archived boolean NOT NULL DEFAULT false;
ALTER TABLE question ADD COLUMN archived boolean NOT NULL DEFAULT false;

CREATE TABLE organization_revision_evidence (
    id uuid PRIMARY KEY,
    revision_id uuid NOT NULL REFERENCES organization_application_revision(id),
    evidence_id uuid NOT NULL REFERENCES organization_evidence(id),
    media_id uuid NOT NULL REFERENCES media_asset(id),
    kind varchar(40) NOT NULL,
    title varchar(180) NOT NULL,
    expires_at timestamptz,
    sha256 varchar(64) NOT NULL,
    status varchar(20) NOT NULL DEFAULT 'UPLOADED' CHECK(status IN ('UPLOADED','ACCEPTED','REJECTED','NEEDS_INFORMATION')),
    reason varchar(1000),
    reviewer_id uuid REFERENCES user_account(id),
    reviewed_at timestamptz,
    UNIQUE(revision_id,evidence_id)
);

-- Existing classifications have no edit API. Capture their current immutable metadata for existing submissions.
INSERT INTO organization_revision_evidence(id,revision_id,evidence_id,media_id,kind,title,expires_at,sha256,status,reason,reviewer_id,reviewed_at)
SELECT md5(r.id::text || ':' || d.id::text)::uuid,r.id,d.id,d.media_id,d.kind,d.title,d.expires_at,m.sha256,d.status,d.reason,d.reviewer_id,d.reviewed_at
FROM organization_application_revision r
JOIN organization_evidence d ON d.organization_id=r.organization_id
    AND d.purpose='VERIFICATION'
    AND d.media_id::text=ANY(string_to_array(r.document_media_ids,','))
JOIN media_asset m ON m.id=d.media_id;

CREATE FUNCTION skillproof_guard_revision_evidence() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
    IF TG_OP='DELETE' THEN RAISE EXCEPTION 'Revision evidence cannot be deleted' USING ERRCODE='23514'; END IF;
    IF (to_jsonb(NEW)-'status'-'reason'-'reviewer_id'-'reviewed_at')
       IS DISTINCT FROM (to_jsonb(OLD)-'status'-'reason'-'reviewer_id'-'reviewed_at')
       OR NOT EXISTS(SELECT 1 FROM organization_application_revision WHERE id=OLD.revision_id AND status='PENDING') THEN
        RAISE EXCEPTION 'Only review fields on a pending revision can change' USING ERRCODE='23514';
    END IF;
    RETURN NEW;
END $$;
CREATE TRIGGER revision_evidence_immutable BEFORE UPDATE OR DELETE ON organization_revision_evidence
FOR EACH ROW EXECUTE FUNCTION skillproof_guard_revision_evidence();

CREATE FUNCTION skillproof_guard_evidence_classification() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
    IF (to_jsonb(NEW)-'status'-'reason'-'reviewer_id'-'reviewed_at')
       IS DISTINCT FROM (to_jsonb(OLD)-'status'-'reason'-'reviewer_id'-'reviewed_at') THEN
        RAISE EXCEPTION 'Evidence classification is immutable; upload and classify a replacement' USING ERRCODE='23514';
    END IF;
    RETURN NEW;
END $$;
CREATE TRIGGER evidence_classification_immutable BEFORE UPDATE ON organization_evidence
FOR EACH ROW EXECUTE FUNCTION skillproof_guard_evidence_classification();

-- Reopening is a narrowly defined PUBLISHED -> DRAFT transition, with no other mutation.
CREATE OR REPLACE FUNCTION skillproof_guard_assessment() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
    IF TG_TABLE_NAME='assessment' THEN
        IF OLD.status='PUBLISHED' THEN
            IF TG_OP='UPDATE' AND NEW.status='DRAFT'
                AND (to_jsonb(NEW)-'status')=(to_jsonb(OLD)-'status')
                AND EXISTS(SELECT 1 FROM course_version WHERE id=OLD.version_id AND status='DRAFT')
                AND NOT EXISTS(SELECT 1 FROM assessment_attempt WHERE assessment_id=OLD.id) THEN
                RETURN NEW;
            END IF;
            RAISE EXCEPTION 'Published assessment is immutable' USING ERRCODE='23514';
        END IF;
    ELSE
        IF EXISTS(SELECT 1 FROM assessment WHERE id=COALESCE(NEW.assessment_id,OLD.assessment_id) AND status='PUBLISHED') THEN
            RAISE EXCEPTION 'Published assessment questions are immutable' USING ERRCODE='23514';
        END IF;
    END IF;
    RETURN COALESCE(NEW,OLD);
END $$;

CREATE FUNCTION skillproof_guard_question_revision_duplicate() RETURNS trigger LANGUAGE plpgsql AS $$
DECLARE root_id uuid;
BEGIN
    SELECT question_id INTO root_id FROM question_version WHERE id=NEW.question_version_id;
    IF EXISTS(SELECT 1 FROM assessment_question aq JOIN question_version qv ON qv.id=aq.question_version_id
        WHERE aq.assessment_id=NEW.assessment_id AND qv.question_id=root_id
          AND (TG_OP='INSERT' OR aq.question_version_id<>OLD.question_version_id)) THEN
        RAISE EXCEPTION 'Only one revision of a question is allowed per assessment' USING ERRCODE='23514';
    END IF;
    IF EXISTS(SELECT 1 FROM question q JOIN question_bank b ON b.id=q.bank_id
              WHERE q.id=root_id AND (q.archived OR b.archived))
       AND NOT EXISTS(SELECT 1 FROM assessment_question previous
            JOIN assessment source ON source.id=previous.assessment_id
            JOIN course_version sv ON sv.id=source.version_id
            JOIN assessment target ON target.id=NEW.assessment_id
            JOIN course_version tv ON tv.id=target.version_id
            WHERE previous.question_version_id=NEW.question_version_id AND sv.course_id=tv.course_id
                AND sv.status IN ('PUBLISHED','ARCHIVED') AND source.status='PUBLISHED') THEN
        RAISE EXCEPTION 'Cannot attach an archived question or bank' USING ERRCODE='23514';
    END IF;
    RETURN NEW;
END $$;
CREATE TRIGGER assessment_unique_question_root BEFORE INSERT OR UPDATE ON assessment_question
FOR EACH ROW EXECUTE FUNCTION skillproof_guard_question_revision_duplicate();

ALTER TABLE assessment ADD CONSTRAINT assessment_required_official CHECK (NOT required_for_completion OR kind='OFFICIAL');

-- Discover the legacy unnamed status CHECK by its definition, not a PostgreSQL-generated name.
DO $$ DECLARE item record; BEGIN
    FOR item IN SELECT conname FROM pg_constraint
        WHERE conrelid='payment_order'::regclass AND contype='c'
          AND pg_get_constraintdef(oid) LIKE '%status%PENDING%PAID%FAILED%CANCELLED%'
    LOOP EXECUTE format('ALTER TABLE payment_order DROP CONSTRAINT %I',item.conname); END LOOP;
END $$;
ALTER TABLE payment_order ADD CONSTRAINT payment_order_status_allowed
    CHECK(status IN ('PENDING','PAID','FAILED','CANCELLED','EXPIRED'));
CREATE INDEX ix_payment_pending_expiry ON payment_order(expires_at) WHERE status='PENDING';

-- A settled result cannot be altered; EXPIRED may receive an authenticated, delayed provider result.
CREATE FUNCTION skillproof_guard_payment_settlement() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
    IF (to_jsonb(NEW)-'status'-'provider_transaction'-'settled_at')
        IS DISTINCT FROM (to_jsonb(OLD)-'status'-'provider_transaction'-'settled_at')
        OR (OLD.status IN ('PAID','FAILED','CANCELLED') AND to_jsonb(NEW) IS DISTINCT FROM to_jsonb(OLD))
        OR (OLD.status='EXPIRED' AND NEW.status='PENDING') THEN
        RAISE EXCEPTION 'Payment amount and finalized provider result are immutable' USING ERRCODE='23514';
    END IF;
    RETURN NEW;
END $$;
CREATE TRIGGER payment_settlement_final BEFORE UPDATE ON payment_order
FOR EACH ROW EXECUTE FUNCTION skillproof_guard_payment_settlement();

-- A finalized assessment cannot accept changed answers or result data.
CREATE FUNCTION skillproof_guard_attempt_final() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
    IF OLD.status IN ('SCORED','TIMED_OUT') AND to_jsonb(NEW) IS DISTINCT FROM to_jsonb(OLD) THEN
        RAISE EXCEPTION 'Final attempt is immutable' USING ERRCODE='23514';
    END IF;
    RETURN NEW;
END $$;
CREATE TRIGGER attempt_result_final BEFORE UPDATE ON assessment_attempt FOR EACH ROW EXECUTE FUNCTION skillproof_guard_attempt_final();

CREATE FUNCTION skillproof_guard_answer_final() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
    IF NOT EXISTS(SELECT 1 FROM assessment_attempt WHERE id=NEW.attempt_id AND status='IN_PROGRESS') THEN
        RAISE EXCEPTION 'Answers require an in-progress attempt' USING ERRCODE='23514';
    END IF;
    RETURN NEW;
END $$;
CREATE TRIGGER attempt_answer_final BEFORE UPDATE ON attempt_question FOR EACH ROW EXECUTE FUNCTION skillproof_guard_answer_final();

CREATE FUNCTION skillproof_guard_application_snapshot() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
    IF (to_jsonb(NEW)-'status'-'reviewer_user_id'-'review_reason'-'reviewed_at')
       IS DISTINCT FROM (to_jsonb(OLD)-'status'-'reviewer_user_id'-'review_reason'-'reviewed_at')
       OR (OLD.status<>'PENDING' AND to_jsonb(NEW) IS DISTINCT FROM to_jsonb(OLD)) THEN
        RAISE EXCEPTION 'Application snapshot is immutable and review decision is final' USING ERRCODE='23514';
    END IF;
    RETURN NEW;
END $$;
CREATE TRIGGER application_snapshot_final BEFORE UPDATE ON organization_application_revision
FOR EACH ROW EXECUTE FUNCTION skillproof_guard_application_snapshot();

ALTER TABLE course_version ADD COLUMN domain_tag varchar(80) NOT NULL DEFAULT 'general';
ALTER TABLE course_version ADD COLUMN difficulty varchar(16) NOT NULL DEFAULT 'UNSPECIFIED'
    CHECK(difficulty IN ('UNSPECIFIED','BEGINNER','INTERMEDIATE','ADVANCED'));
ALTER TABLE course_version ADD COLUMN skills jsonb NOT NULL DEFAULT '[]'::jsonb CHECK(jsonb_typeof(skills)='array' AND jsonb_array_length(skills)<=20);
ALTER TABLE course_version ADD COLUMN prerequisite_text varchar(1000) NOT NULL DEFAULT '';

CREATE TABLE learning_signal (
    id uuid PRIMARY KEY,
    learner_id uuid NOT NULL REFERENCES user_account(id),
    course_version_id uuid NOT NULL REFERENCES course_version(id),
    kind varchar(32) NOT NULL CHECK(kind IN ('ENROLLED','RESOURCE_COMPLETED','LESSON_COMPLETED','ASSESSMENT_FINALIZED','ASSIGNMENT_SUBMITTED','ASSIGNMENT_GRADED','COURSE_COMPLETED')),
    reference_id uuid NOT NULL,
    schema_version integer NOT NULL CHECK(schema_version=1),
    occurred_at timestamptz NOT NULL,
    details jsonb NOT NULL,
    UNIQUE(learner_id,course_version_id,kind,reference_id)
);
CREATE INDEX ix_learning_signal_learner_time ON learning_signal(learner_id,occurred_at);
CREATE INDEX ix_learning_signal_version_time ON learning_signal(course_version_id,occurred_at);
CREATE TRIGGER learning_signal_final BEFORE UPDATE OR DELETE ON learning_signal FOR EACH ROW EXECUTE FUNCTION skillproof_immutable_audit_evidence();
