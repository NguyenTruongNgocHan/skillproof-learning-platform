CREATE FUNCTION skillproof_require_draft(version_id UUID) RETURNS void LANGUAGE plpgsql AS $$
BEGIN
    IF NOT EXISTS(SELECT 1 FROM course_version v WHERE v.id=version_id AND v.status='DRAFT') THEN
        RAISE EXCEPTION 'Course version must be draft' USING ERRCODE='23514';
    END IF;
END $$;
CREATE FUNCTION skillproof_guard_version_content() RETURNS trigger LANGUAGE plpgsql AS $$
DECLARE target UUID;
BEGIN
    IF TG_OP <> 'INSERT' THEN
        IF TG_TABLE_NAME='course_module' THEN target := OLD.version_id;
        ELSE SELECT version_id INTO target FROM course_module WHERE id=OLD.module_id; END IF;
        PERFORM skillproof_require_draft(target);
    END IF;
    IF TG_OP <> 'DELETE' THEN
        IF TG_TABLE_NAME='course_module' THEN target := NEW.version_id;
        ELSE SELECT version_id INTO target FROM course_module WHERE id=NEW.module_id; END IF;
        PERFORM skillproof_require_draft(target);
    END IF;
    RETURN COALESCE(NEW,OLD);
END $$;
CREATE TRIGGER guard_module
BEFORE INSERT OR UPDATE OR DELETE
ON course_module
FOR EACH ROW
EXECUTE FUNCTION skillproof_guard_version_content();


CREATE TRIGGER guard_resource
BEFORE INSERT OR UPDATE OR DELETE
ON course_resource
FOR EACH ROW
EXECUTE FUNCTION skillproof_guard_version_content();


CREATE FUNCTION skillproof_guard_used_question()
RETURNS trigger
LANGUAGE plpgsql
AS $$
DECLARE
    qv UUID;
BEGIN
    qv := CASE
        WHEN TG_TABLE_NAME = 'question_option'
            THEN COALESCE(NEW.question_version_id, OLD.question_version_id)
        ELSE COALESCE(NEW.id, OLD.id)
    END;

    IF EXISTS (
        SELECT 1
        FROM assessment_question
        WHERE question_version_id = qv
    )
    OR EXISTS (
        SELECT 1
        FROM attempt_question
        WHERE question_version_id = qv
    ) THEN
        RAISE EXCEPTION 'Used question version is immutable' USING ERRCODE='23514';
    END IF;

    RETURN COALESCE(NEW, OLD);
END
$$;


CREATE TRIGGER guard_question_version
BEFORE UPDATE OR DELETE
ON question_version
FOR EACH ROW
EXECUTE FUNCTION skillproof_guard_used_question();


CREATE TRIGGER guard_question_option
BEFORE INSERT OR UPDATE OR DELETE
ON question_option
FOR EACH ROW
EXECUTE FUNCTION skillproof_guard_used_question();





CREATE FUNCTION skillproof_guard_progress_version()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM enrollment e
        JOIN course_module m
            ON m.version_id = e.version_id
        JOIN course_resource r
            ON r.module_id = m.id
        WHERE e.id = NEW.enrollment_id
          AND r.id = NEW.resource_id
    ) THEN
        RAISE EXCEPTION 'Resource is outside the enrolled version' USING ERRCODE='23514';
    END IF;

    RETURN NEW;
END
$$;


CREATE TRIGGER guard_progress_version
BEFORE INSERT OR UPDATE
ON resource_progress
FOR EACH ROW
EXECUTE FUNCTION skillproof_guard_progress_version();


CREATE FUNCTION skillproof_guard_policy()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM course_version
        WHERE id = OLD.version_id
          AND status <> 'DRAFT'
    ) THEN
        RAISE EXCEPTION 'Published completion policy is immutable' USING ERRCODE='23514';
    END IF;

    RETURN COALESCE(NEW, OLD);
END
$$;


CREATE TRIGGER guard_completion_policy
BEFORE UPDATE OR DELETE
ON completion_policy
FOR EACH ROW
EXECUTE FUNCTION skillproof_guard_policy();


CREATE FUNCTION skillproof_guard_assessment()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    IF TG_TABLE_NAME = 'assessment' THEN
        IF OLD.status = 'PUBLISHED' THEN
            RAISE EXCEPTION 'Published assessment is immutable' USING ERRCODE='23514';
        END IF;
    ELSE
        IF EXISTS (
            SELECT 1
            FROM assessment
            WHERE id = COALESCE(
                NEW.assessment_id,
                OLD.assessment_id
            )
              AND status = 'PUBLISHED'
        ) THEN
            RAISE EXCEPTION
                'Published assessment questions are immutable' USING ERRCODE='23514';
        END IF;
    END IF;

    RETURN COALESCE(NEW, OLD);
END
$$;


CREATE TRIGGER guard_assessment
BEFORE UPDATE OR DELETE
ON assessment
FOR EACH ROW
EXECUTE FUNCTION skillproof_guard_assessment();


CREATE TRIGGER guard_assessment_questions
BEFORE INSERT OR UPDATE OR DELETE
ON assessment_question
FOR EACH ROW
EXECUTE FUNCTION skillproof_guard_assessment();


CREATE FUNCTION skillproof_guard_assessment_context()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM course_version v
        JOIN course p
            ON p.id = v.course_id
        WHERE v.id = NEW.version_id
          AND p.organization_id IS NOT DISTINCT FROM NEW.organization_id
    ) THEN
        RAISE EXCEPTION
            'Assessment organization differs from course owner' USING ERRCODE='23514';
    END IF;

    RETURN NEW;
END
$$;


CREATE TRIGGER guard_assessment_context
BEFORE INSERT OR UPDATE
ON assessment
FOR EACH ROW
EXECUTE FUNCTION skillproof_guard_assessment_context();


CREATE FUNCTION skillproof_guard_assessment_question_context()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM assessment a
        JOIN question_version v
            ON v.id = NEW.question_version_id
        JOIN question q
            ON q.id = v.question_id
        JOIN question_bank b
            ON b.id = q.bank_id
        WHERE a.id = NEW.assessment_id
          AND a.organization_id IS NOT DISTINCT FROM b.organization_id
          AND (a.organization_id IS NOT NULL OR b.created_by = (SELECT c.created_by FROM course c JOIN course_version cv ON cv.course_id=c.id WHERE cv.id=a.version_id))
    ) THEN
        RAISE EXCEPTION
            'Question belongs to another organization' USING ERRCODE='23514';
    END IF;

    RETURN NEW;
END
$$;


CREATE TRIGGER guard_assessment_question_context
BEFORE INSERT OR UPDATE
ON assessment_question
FOR EACH ROW
EXECUTE FUNCTION skillproof_guard_assessment_question_context();


CREATE FUNCTION skillproof_guard_attempt_context()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM assessment a
        JOIN enrollment e
            ON e.version_id = a.version_id
        WHERE a.id = NEW.assessment_id
          AND e.id = NEW.enrollment_id
          AND e.learner_id = NEW.learner_id
    ) THEN
        RAISE EXCEPTION
            'Attempt does not match enrollment, learner or version' USING ERRCODE='23514';
    END IF;

    RETURN NEW;
END
$$;


CREATE TRIGGER guard_attempt_context
BEFORE INSERT
ON assessment_attempt
FOR EACH ROW
EXECUTE FUNCTION skillproof_guard_attempt_context();


CREATE FUNCTION skillproof_guard_attempt_question_context()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM assessment_attempt t
        JOIN assessment_question aq
            ON aq.assessment_id = t.assessment_id
        WHERE t.id = NEW.attempt_id
          AND aq.question_version_id = NEW.question_version_id
    ) THEN
        RAISE EXCEPTION
            'Attempt question is outside assessment snapshot' USING ERRCODE='23514';
    END IF;

    RETURN NEW;
END
$$;


CREATE TRIGGER guard_attempt_question_context
BEFORE INSERT
ON attempt_question
FOR EACH ROW
EXECUTE FUNCTION skillproof_guard_attempt_question_context();



CREATE TRIGGER guard_lesson BEFORE INSERT OR UPDATE OR DELETE ON course_lesson FOR EACH ROW EXECUTE FUNCTION skillproof_guard_version_content();
CREATE FUNCTION skillproof_guard_version_snapshot() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
    IF OLD.status <> 'DRAFT' AND (
        (to_jsonb(NEW)-'status') IS DISTINCT FROM (to_jsonb(OLD)-'status')
        OR NOT(OLD.status='PUBLISHED' AND NEW.status='ARCHIVED' OR NEW.status=OLD.status)) THEN
        RAISE EXCEPTION 'Published course version snapshot is immutable' USING ERRCODE='23514';
    END IF;
    RETURN NEW;
END $$;
CREATE TRIGGER guard_version_snapshot BEFORE UPDATE ON course_version FOR EACH ROW EXECUTE FUNCTION skillproof_guard_version_snapshot();
CREATE FUNCTION skillproof_validate_activity_owner(target_version UUID, target_scope VARCHAR, target_owner UUID) RETURNS void LANGUAGE plpgsql AS $$
BEGIN
    IF NOT ((target_scope='COURSE' AND target_owner=target_version)
        OR (target_scope='MODULE' AND EXISTS(SELECT 1 FROM course_module m WHERE m.id=target_owner AND m.version_id=target_version))
        OR (target_scope='LESSON' AND EXISTS(SELECT 1 FROM course_lesson l JOIN course_module m ON l.module_id=m.id WHERE l.id=target_owner AND m.version_id=target_version))) THEN
        RAISE EXCEPTION 'Activity owner must belong to its course version' USING ERRCODE='23514';
    END IF;
END $$;
CREATE FUNCTION skillproof_guard_activity_owner() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
    IF TG_OP <> 'DELETE' THEN
        PERFORM skillproof_require_draft(NEW.version_id);
        PERFORM skillproof_validate_activity_owner(NEW.version_id,NEW.owner_scope,NEW.owner_id);
    END IF;
    IF TG_OP <> 'INSERT' THEN PERFORM skillproof_require_draft(OLD.version_id); END IF;
    RETURN COALESCE(NEW,OLD);
END $$;
CREATE TRIGGER guard_assignment_owner BEFORE INSERT OR UPDATE OR DELETE ON assignment FOR EACH ROW EXECUTE FUNCTION skillproof_guard_activity_owner();
CREATE TRIGGER guard_assessment_owner BEFORE INSERT OR UPDATE OR DELETE ON assessment FOR EACH ROW EXECUTE FUNCTION skillproof_guard_activity_owner();
CREATE FUNCTION skillproof_guard_submission() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
    IF TG_OP='INSERT' AND NOT EXISTS(SELECT 1 FROM assignment a JOIN enrollment e ON a.version_id=e.version_id
      WHERE a.id=NEW.assignment_id AND e.id=NEW.enrollment_id AND e.learner_id=NEW.learner_id) THEN RAISE EXCEPTION 'Submission enrollment mismatch' USING ERRCODE='23514'; END IF;
    IF TG_OP='UPDATE' AND (OLD.status='GRADED' OR OLD.assignment_id<>NEW.assignment_id OR OLD.enrollment_id<>NEW.enrollment_id OR OLD.learner_id<>NEW.learner_id) THEN
        RAISE EXCEPTION 'Final submission/context is immutable' USING ERRCODE='23514';
    END IF;
    RETURN NEW;
END $$;
CREATE TRIGGER guard_submission BEFORE INSERT OR UPDATE ON assignment_submission FOR EACH ROW EXECUTE FUNCTION skillproof_guard_submission();
CREATE FUNCTION skillproof_guard_lesson_progress() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
    IF NOT EXISTS(SELECT 1 FROM enrollment e JOIN course_module m ON m.version_id=e.version_id JOIN course_lesson l ON l.module_id=m.id
       WHERE e.id=NEW.enrollment_id AND l.id=NEW.lesson_id) THEN RAISE EXCEPTION 'Lesson is outside enrollment' USING ERRCODE='23514'; END IF;
    RETURN NEW;
END $$;
CREATE TRIGGER guard_lesson_progress BEFORE INSERT OR UPDATE ON lesson_progress FOR EACH ROW EXECUTE FUNCTION skillproof_guard_lesson_progress();
CREATE FUNCTION skillproof_guard_product() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
    IF (NEW.product_type IN ('COURSE','CERTIFICATION') AND NOT EXISTS(SELECT 1 FROM course_version WHERE id=NEW.product_id))
       OR (NEW.product_type='RESOURCE' AND NOT EXISTS(SELECT 1 FROM library_resource WHERE id=NEW.product_id)) THEN
        RAISE EXCEPTION 'Product does not exist' USING ERRCODE='23514';
    END IF;
    RETURN NEW;
END $$;
CREATE TRIGGER guard_access_product BEFORE INSERT OR UPDATE ON access_grant FOR EACH ROW EXECUTE FUNCTION skillproof_guard_product();
CREATE TRIGGER guard_payment_product BEFORE INSERT OR UPDATE ON payment_order FOR EACH ROW EXECUTE FUNCTION skillproof_guard_product();
CREATE FUNCTION skillproof_guard_path_item() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
    IF EXISTS(SELECT 1 FROM course_version WHERE id=NEW.course_version_id AND status='DRAFT') THEN RAISE EXCEPTION 'Path requires published course' USING ERRCODE='23514'; END IF;
    IF NEW.prerequisite_item_id IS NOT NULL AND NOT EXISTS(SELECT 1 FROM learning_path_item WHERE id=NEW.prerequisite_item_id AND path_id=NEW.path_id AND position<NEW.position) THEN
        RAISE EXCEPTION 'Prerequisite must precede this path item' USING ERRCODE='23514';
    END IF;
    RETURN NEW;
END $$;
CREATE TRIGGER guard_path_item BEFORE INSERT OR UPDATE ON learning_path_item FOR EACH ROW EXECUTE FUNCTION skillproof_guard_path_item();
CREATE FUNCTION skillproof_guard_library_snapshot() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
    IF OLD.status IN ('PUBLISHED','WITHDRAWN') AND (to_jsonb(NEW)-'status') IS DISTINCT FROM (to_jsonb(OLD)-'status') THEN
       RAISE EXCEPTION 'Published library content is immutable' USING ERRCODE='23514';
    END IF;
    RETURN NEW;
END $$;
CREATE TRIGGER guard_library_snapshot BEFORE UPDATE ON library_resource FOR EACH ROW EXECUTE FUNCTION skillproof_guard_library_snapshot();
CREATE FUNCTION skillproof_guard_media_target() RETURNS trigger LANGUAGE plpgsql AS $$
DECLARE target UUID;
BEGIN
    IF NEW.scope='RESOURCE' THEN
        SELECT m.version_id INTO target FROM course_resource r JOIN course_module m ON r.module_id=m.id WHERE r.id=NEW.resource_id;
        PERFORM skillproof_require_draft(target);
    ELSIF NEW.scope='LIBRARY' AND NOT EXISTS(SELECT 1 FROM library_resource WHERE id=NEW.library_resource_id AND status='DRAFT') THEN
        RAISE EXCEPTION 'Library attachments require draft' USING ERRCODE='23514';
    ELSIF NEW.scope='SUBMISSION' AND NOT EXISTS(SELECT 1 FROM assignment_submission WHERE id=NEW.submission_id AND status='DRAFT') THEN
        RAISE EXCEPTION 'Submission attachments require draft' USING ERRCODE='23514';
    END IF;
    RETURN NEW;
END $$;
CREATE TRIGGER guard_media_target BEFORE INSERT ON media_asset FOR EACH ROW EXECUTE FUNCTION skillproof_guard_media_target();
