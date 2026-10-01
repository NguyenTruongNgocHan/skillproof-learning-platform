package com.skillproof.backend.quiz.application;

import com.skillproof.backend.common.exception.*;
import com.skillproof.backend.learning.contract.LearningQuizAccess;
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
    private final LearningQuizAccess learning;

    public AssessmentAuthoringService(QuizAssessmentRepository a, QuizAssessmentQuestionRepository l,
            QuizQuestionVersionRepository v, QuizQuestionRootRepository q, QuizBankRepository b, LearningQuizAccess learning) {
        assessments = a;
        links = l;
        versions = v;
        questions = q;
        banks = b;
        this.learning = learning;
    }

    private QuizAssessment assessmentEntity(UUID id) {
        return assessments.findById(id).orElseThrow(() -> new NotFoundException("QUIZ_NOT_FOUND", "Quiz record not found"));
    }

    private Map<String, Object> assessment(UUID id) {
        var a = assessmentEntity(id);
        var out = new LinkedHashMap<String, Object>();
        out.put("id", a.id);
        out.put("organization_id", a.organizationId);
        out.put("version_id", a.versionId);
        out.put("kind", a.kind.name());
        out.put("status", a.status.name());
        out.put("title", a.title);
        out.put("duration_seconds", a.durationSeconds);
        out.put("pass_percent", a.passPercent);
        out.put("max_attempts", a.maxAttempts);
        out.put("created_at", a.createdAt);
        out.put("version_status", learning.version(a.versionId).status());
        return out;
    }

    private QuizAssessment editable(UUID actor, UUID id) {
        var a = assessmentEntity(id);
        learning.requireOrganizer(actor, a.organizationId);
        if (a.status != QuizAssessment.Status.DRAFT || !"DRAFT".equals(learning.version(a.versionId).status())) {
            throw new ConflictException("ASSESSMENT_IMMUTABLE", "Published assessment cannot be edited");
        
        }return a;
    }

    public List<Map<String, Object>> assessments(UUID actor, UUID versionId) {
        var v = learning.version(versionId);
        learning.requireOrganizer(actor, v.organizationId());
        return assessments.findByVersionIdOrderByCreatedAtDesc(versionId).stream().map(a -> assessment(a.id)).toList();
    }

    public List<Map<String, Object>> assessmentQuestions(UUID actor, UUID id) {
        var a = assessmentEntity(id);
        learning.requireOrganizer(actor, a.organizationId);
        return links.findByAssessmentIdOrderByPosition(id).stream().map(x -> Map.<String, Object>of("question_version_id", x.questionVersion.id, "position", x.position, "points", x.points, "stem", x.questionVersion.stem)).toList();
    }

    @Transactional
    public Map<String, Object> createAssessment(UUID actor, UUID versionId, String kind, String title, int duration, int pass, int max) {
        var v = learning.version(versionId);
        learning.requireOrganizer(actor, v.organizationId());
        if (!"DRAFT".equals(v.status()) || !List.of("PRACTICE", "MOCK", "OFFICIAL").contains(kind) || duration < 60 || duration > 14400 || pass < 1 || pass > 100 || max < 1 || max > 20) {
            throw new BadRequestException("ASSESSMENT_INVALID", "Invalid assessment policy");
        }
        var a = new QuizAssessment();
        a.id = UUID.randomUUID();
        a.organizationId = v.organizationId();
        a.versionId = versionId;
        a.kind = QuizAssessment.Kind.valueOf(kind);
        a.status = QuizAssessment.Status.DRAFT;
        a.title = title.trim();
        a.durationSeconds = duration;
        a.passPercent = pass;
        a.maxAttempts = max;
        a.createdAt = Instant.now();
        return assessment(assessments.save(a).id);
    }

    @Transactional
    public Map<String, Object> updateAssessment(UUID actor, UUID id, String title, int duration, int pass, int max) {
        var a = editable(actor, id);
        if (duration < 60 || duration > 14400 || pass < 1 || pass > 100 || max < 1 || max > 20) {
            throw new BadRequestException("ASSESSMENT_INVALID", "Invalid assessment policy");
        
        }a.title = title.trim();
        a.durationSeconds = duration;
        a.passPercent = pass;
        a.maxAttempts = max;
        assessments.save(a);
        return assessment(id);
    }

    @Transactional
    public void attach(UUID actor, UUID aid, UUID qid, int position, int points) {
        var a = editable(actor, aid);
        var qv = versions.findById(qid).orElseThrow(() -> new NotFoundException("QUIZ_NOT_FOUND", "Question not found"));
        var q = questions.findById(qv.questionId).orElseThrow();
        var b = banks.findById(q.bankId).orElseThrow();
        if (!b.organizationId.equals(a.organizationId)) {
            throw new BadRequestException("QUESTION_OWNERSHIP", "Question belongs to another organization");
        
        }if (position < 1 || points < 1 || points > 100) {
            throw new BadRequestException("QUESTION_WEIGHT", "Invalid question position or points");
        
        }var x = new QuizAssessmentQuestion();
        x.id = new QuizAssessmentQuestionId(aid, qid);
        x.assessment = a;
        x.questionVersion = qv;
        x.position = position;
        x.points = points;
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
        
        }a.status = QuizAssessment.Status.PUBLISHED;
        assessments.save(a);
        return assessment(id);
    }
}
