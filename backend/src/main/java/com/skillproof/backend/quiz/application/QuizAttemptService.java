package com.skillproof.backend.quiz.application;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillproof.backend.common.exception.BadRequestException;
import com.skillproof.backend.common.exception.ConflictException;
import com.skillproof.backend.common.exception.NotFoundException;
import com.skillproof.backend.learning.contract.LearningQuizAccess;
import com.skillproof.backend.quiz.contract.QuizCompletionEvidenceQuery;

@Service
public class QuizAttemptService {

    private final QuizAssessmentRepository assessments;
    private final QuizAttemptRepository attempts;
    private final QuizAttemptQuestionRepository questions;
    private final QuizAssessmentQuestionRepository assessmentQuestions;
    private final QuizOptionRepository options;
    private final QuizQuestionRepository questionVersions;
    private final LearningQuizAccess learning;
    private final QuizCompletionEvidenceQuery evidence;

    public QuizAttemptService(QuizAssessmentRepository a, QuizAttemptRepository t, QuizAttemptQuestionRepository q,
            LearningQuizAccess l, QuizCompletionEvidenceQuery e, QuizAssessmentQuestionRepository aq,
            QuizOptionRepository o, QuizQuestionRepository qv) {
        assessments = a;
        attempts = t;
        questions = q;
        learning = l;
        evidence = e;
        assessmentQuestions = aq;
        options = o;
        questionVersions = qv;
    }

    private QuizAttempt attempt(UUID learner, UUID id) {
        var a = attempts.findById(id).orElseThrow(() -> new NotFoundException("QUIZ_NOT_FOUND", "Quiz record not found"));
        if (!learner.equals(a.learnerId)) {
            throw new NotFoundException("QUIZ_NOT_FOUND", "Quiz record not found");
        
        }return a;
    }

    public List<Map<String, Object>> forEnrollment(UUID learner, UUID enrollmentId) {
        var e = learning.enrollment(learner, enrollmentId, false);
        return assessments.findByVersionIdAndStatus(e.versionId(), QuizAssessment.Status.PUBLISHED).stream().map(a -> Map.<String, Object>of("id", a.id, "version_id", a.versionId, "kind", a.kind.name(), "title", a.title, "duration_seconds", a.durationSeconds, "pass_percent", a.passPercent, "max_attempts", a.maxAttempts)).toList();
    }

    public List<Map<String, Object>> attemptHistory(UUID learner, UUID enrollmentId) {
        learning.enrollment(learner, enrollmentId, false);
        return attempts.findByEnrollmentIdAndLearnerIdOrderByStartedAtDesc(enrollmentId, learner).stream().map(a -> Map.<String, Object>of("id", a.id, "assessment_id", a.assessmentId, "status", a.status.name(), "score_percent", a.scorePercent, "passed", a.passed, "started_at", a.startedAt, "deadline_at", a.deadlineAt, "submitted_at", a.submittedAt)).toList();
    }

    @Transactional
    public Map<String, Object> start(UUID learner, UUID assessmentId, UUID enrollmentId) {
        var e = learning.enrollment(learner, enrollmentId, true);
        var a = assessments.findById(assessmentId).orElseThrow(() -> new NotFoundException("ASSESSMENT_UNAVAILABLE", "Assessment is not available in this enrollment"));
        if (a.status != QuizAssessment.Status.PUBLISHED || !a.versionId.equals(e.versionId())) {
            throw new NotFoundException("ASSESSMENT_UNAVAILABLE", "Assessment is not available in this enrollment");
        }
        var active = attempts.findByAssessmentIdAndEnrollmentIdAndStatus(assessmentId, enrollmentId, QuizAttempt.AttemptStatus.IN_PROGRESS).stream().findFirst();
        if (active.isPresent() && active.get().deadlineAt.isAfter(Instant.now())) {
            return view(active.get());
        }
        if (attempts.countByAssessmentIdAndEnrollmentId(assessmentId, enrollmentId) >= a.maxAttempts) {
            throw new ConflictException("ATTEMPT_LIMIT", "No attempts remaining");
        }
        var t = new QuizAttempt();
        t.id = UUID.randomUUID();
        t.assessmentId = assessmentId;
        t.enrollmentId = enrollmentId;
        t.learnerId = learner;
        t.status = QuizAttempt.AttemptStatus.IN_PROGRESS;
        t.startedAt = Instant.now();
        t.deadlineAt = t.startedAt.plusSeconds(a.durationSeconds);
        attempts.save(t);
        for (var aq : assessmentQuestions.findByAssessmentIdOrderByPosition(assessmentId)) {
            var x = new QuizAttemptQuestion();
            x.id = new QuizAttemptQuestionId(t.id, aq.questionVersion.id);
            x.position = aq.position;
            x.points = aq.points;
            questions.save(x);
        }
        return view(t);
    }

    private Map<String, Object> view(QuizAttempt t) {
        var items = questions.findByIdAttemptIdOrderByPosition(t.id).stream().map(x -> {
            var q = questionVersions.findById(x.id.questionVersionId).orElseThrow();
            return Map.<String, Object>of("question_version_id", q.id, "position", x.position, "points", x.points, "selected_option_id", x.selectedOptionId, "stem", q.stem, "options", options.findByQuestionVersionIdOrderByPosition(q.id).stream().map(o -> Map.<String, Object>of("id", o.id, "position", o.position, "body", o.body)).toList());
        }).toList();
        return Map.of("attempt", Map.of("id", t.id, "assessment_id", t.assessmentId, "status", t.status.name(), "started_at", t.startedAt, "deadline_at", t.deadlineAt), "questions", items);
    }

    @Transactional
    public Map<String, Object> answer(UUID learner, UUID id, UUID question, UUID option) {
        var t = attempt(learner, id);
        if (t.status != QuizAttempt.AttemptStatus.IN_PROGRESS) {
            throw new ConflictException("ATTEMPT_FINAL", "Attempt already finalized");
        
        }if (!t.deadlineAt.isAfter(Instant.now())) {
            return submit(learner, id);
        
        }var x = questions.findById(new QuizAttemptQuestionId(id, question)).orElseThrow(() -> new BadRequestException("ANSWER_INVALID", "Question or option does not belong to this attempt"));
        if (options.findByQuestionVersionIdOrderByPosition(question).stream().noneMatch(o -> o.id.equals(option))) {
            throw new BadRequestException("ANSWER_INVALID", "Question or option does not belong to this attempt");
        
        }x.selectedOptionId = option;
        questions.save(x);
        return view(t);
    }

    @Transactional
    public Map<String, Object> submit(UUID learner, UUID id) {
        var t = attempt(learner, id);
        if (t.status == QuizAttempt.AttemptStatus.SCORED) {
            return result(learner, id);
        
        }int total = 0, earned = 0;
        for (var x : questions.findByIdAttemptIdOrderByPosition(id)) {
            total += x.points;
            if (x.selectedOptionId != null && options.findById(x.selectedOptionId).map(o -> o.correct).orElse(false)) {
                earned += x.points;
        
            }}
        var a = assessments.findById(t.assessmentId).orElseThrow();
        t.status = QuizAttempt.AttemptStatus.SCORED;
        t.submittedAt = Instant.now();
        t.scorePercent = total == 0 ? 0 : earned * 100 / total;
        t.passed = t.scorePercent >= a.passPercent;
        attempts.save(t);
        if (t.passed && a.kind == QuizAssessment.Kind.OFFICIAL) {
            var ev = evidence.evidence(a.versionId, t.enrollmentId);
            learning.evaluateCompletion(t.enrollmentId, ev.requiredOfficialAssessments(), ev.passedOfficialAssessments());
        }
        return result(learner, id);
    }

    public Map<String, Object> result(UUID learner, UUID id) {
        var t = attempt(learner, id);
        if (t.status != QuizAttempt.AttemptStatus.SCORED) {
            throw new ConflictException("RESULT_NOT_READY", "Submit the attempt to see its result");
        
        }return Map.of("attemptId", t.id, "assessmentId", t.assessmentId, "scorePercent", t.scorePercent, "passed", t.passed, "timedOut", t.submittedAt == null);
    }

    public Map<String, Object> resume(UUID learner, UUID id) {
        var t = attempt(learner, id);
        return t.status == QuizAttempt.AttemptStatus.SCORED ? result(learner, id) : view(t);
    }
}
