package com.skillproof.backend.quiz.application;

import com.skillproof.backend.quiz.infrastructure.persistence.*;

import com.skillproof.backend.common.exception.*;
import com.skillproof.backend.course.contract.CourseQuizAccess;
import java.time.Instant;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AssessmentAuthoringService {

    private final QuizAssessmentRepository assessments;
    private final QuizAssessmentQuestionRepository links;
    private final QuizQuestionVersionRepository versions;
    private final QuizQuestionRootRepository questions;
    private final QuizBankRepository banks;
    private final CourseQuizAccess learning;
    private final com.skillproof.backend.course.contract.CourseStructureQuery structure;

    public AssessmentAuthoringService(QuizAssessmentRepository a, QuizAssessmentQuestionRepository l,
            QuizQuestionVersionRepository v, QuizQuestionRootRepository q, QuizBankRepository b, CourseQuizAccess learning, com.skillproof.backend.course.contract.CourseStructureQuery structure) {
        assessments = a;
        links = l;
        versions = v;
        questions = q;
        banks = b;
        this.learning = learning;
        this.structure = structure;
    }

    private QuizAssessment assessmentEntity(UUID id) {
        return assessments.findById(id).orElseThrow(() -> new NotFoundException("QUIZ_NOT_FOUND", "Quiz record not found"));
    }

    private Map<String, Object> assessment(UUID id) {
        var a = assessmentEntity(id);
        var out = new LinkedHashMap<String, Object>();
        out.put("id", a.getId());
        out.put("organization_id", a.getOrganizationId());
        out.put("version_id", a.getVersionId());
        out.put("ownerScope", a.getOwnerScope());
        out.put("ownerId", a.getOwnerId());
        out.put("required", a.isRequiredForCompletion());
        out.put("kind", a.getKind().name());
        out.put("status", a.getStatus().name());
        out.put("title", a.getTitle());
        out.put("duration_seconds", a.getDurationSeconds());
        out.put("pass_percent", a.getPassPercent());
        out.put("max_attempts", a.getMaxAttempts());
        out.put("created_at", a.getCreatedAt());
        out.put("version_status", learning.version(a.getVersionId()).status());
        return out;
    }

    private QuizAssessment editable(UUID actor, UUID id) {
        var versionId = assessments.findVersionId(id).orElseThrow(()
                -> new NotFoundException("QUIZ_NOT_FOUND", "Quiz record not found"));
        var version = learning.lockVersion(versionId);
        var assessment = assessmentEntity(id);
        structure.requireDraftAuthor(actor, versionId);
        if (assessment.getStatus() != QuizAssessment.Status.DRAFT || !"DRAFT".equals(version.status())) {
            throw new ConflictException("ASSESSMENT_IMMUTABLE", "Published assessment cannot be edited");
        }
        return assessment;
    }

    public List<Map<String, Object>> assessments(UUID actor, UUID versionId) {
        var v = learning.version(versionId);
        learning.requireVersionAuthor(actor, versionId);
        return assessments.findByVersionIdOrderByCreatedAtDesc(versionId).stream().map(a -> assessment(a.getId())).toList();
    }

    public List<Map<String, Object>> assessmentQuestions(UUID actor, UUID id) {
        var a = assessmentEntity(id);
        learning.requireVersionAuthor(actor, a.getVersionId());
        return links.findByAssessmentIdOrderByPosition(id).stream().map(x -> Map.<String, Object>of("question_version_id", x.getQuestionVersion().getId(), "position", x.getPosition(), "points", x.getPoints(), "stem", x.getQuestionVersion().getStem())).toList();
    }

    @Transactional
    public Map<String, Object> createAssessment(UUID actor, UUID versionId, String kind, String title, int duration, int pass, int max) {
        var v = learning.lockVersion(versionId);
        learning.requireVersionAuthor(actor, versionId);
        if (!"DRAFT".equals(v.status()) || !List.of("PRACTICE", "MOCK", "OFFICIAL").contains(kind) || duration < 60 || duration > 14400 || pass < 1 || pass > 100 || max < 1 || max > 20) {
            throw new BadRequestException("ASSESSMENT_INVALID", "Invalid assessment policy");
        }
        structure.requireDraftAuthor(actor, versionId);
        var a = new QuizAssessment();
        a.setId(UUID.randomUUID());
        a.setOrganizationId(v.organizationId());
        a.setVersionId(versionId);
        a.setOwnerScope("COURSE");
        a.setOwnerId(versionId);
        a.setRequiredForCompletion("OFFICIAL".equals(kind));
        a.setKind(QuizAssessment.Kind.valueOf(kind));
        a.setStatus(QuizAssessment.Status.DRAFT);
        a.setTitle(title.trim());
        a.setDurationSeconds(duration);
        a.setPassPercent(pass);
        a.setMaxAttempts(max);
        a.setCreatedAt(Instant.now());
        return assessment(assessments.save(a).getId());
    }

    @Transactional
    public Map<String, Object> updateAssessment(UUID actor, UUID id, String title, int duration, int pass, int max) {
        var a = editable(actor, id);
        if (duration < 60 || duration > 14400 || pass < 1 || pass > 100 || max < 1 || max > 20) {
            throw new BadRequestException("ASSESSMENT_INVALID", "Invalid assessment policy");

        }
        a.setTitle(title.trim());
        a.setDurationSeconds(duration);
        a.setPassPercent(pass);
        a.setMaxAttempts(max);
        assessments.save(a);
        return assessment(id);
    }

    @Transactional
    public void attach(UUID actor, UUID aid, UUID qid, int position, int points) {
        var a = editable(actor, aid);
        var qv = versions.findById(qid).orElseThrow(() -> new NotFoundException("QUIZ_NOT_FOUND", "Question not found"));
        var q = questions.findById(qv.getQuestionId()).orElseThrow();
        var b = banks.findById(q.getBankId()).orElseThrow();
        if (!Objects.equals(b.getOrganizationId(), a.getOrganizationId())
                || a.getOrganizationId() == null && !b.getCreatedBy().equals(learning.versionAuthor(a.getVersionId()))) {
            throw new BadRequestException("QUESTION_OWNERSHIP", "Question belongs to another organization");

        }
        if (position < 1 || points < 1 || points > 100) {
            throw new BadRequestException("QUESTION_WEIGHT", "Invalid question position or points");

        }
        var x = new QuizAssessmentQuestion();
        x.setId(new QuizAssessmentQuestionId(aid, qid));
        x.setAssessment(a);
        x.setQuestionVersion(qv);
        x.setPosition(position);
        x.setPoints(points);
        links.save(x);
    }

    @Transactional
    public void detach(UUID actor, UUID aid, UUID qid) {
        editable(actor, aid);
        links.deleteById(new QuizAssessmentQuestionId(aid, qid));
    }

    @Transactional
    public void deleteAssessment(UUID actor, UUID id) {
        editable(actor, id);
        links.deleteByAssessmentId(id);
        assessments.deleteById(id);
    }

    @Transactional
    public Map<String, Object> publish(UUID actor, UUID id) {
        var a = editable(actor, id);
        if (links.findByAssessmentIdOrderByPosition(id).isEmpty()) {
            throw new BadRequestException("ASSESSMENT_EMPTY", "Add questions before publishing");

        }
        a.setStatus(QuizAssessment.Status.PUBLISHED);
        assessments.save(a);
        return assessment(id);
    }

    @Transactional
    public Map<String, Object> configureOwner(UUID actor, UUID id, String scope, UUID owner, boolean required) {
        var assessment = editable(actor, id);
        structure.validateOwner(assessment.getVersionId(), scope, owner);
        if (required && assessment.getKind() != QuizAssessment.Kind.OFFICIAL) {
            throw new BadRequestException("PRACTICE_OPTIONAL", "Practice and mock assessments are optional");
        }
        assessment.setOwnerScope(scope);
        assessment.setOwnerId(owner);
        assessment.setRequiredForCompletion(required);
        return assessment(id);
    }

}
