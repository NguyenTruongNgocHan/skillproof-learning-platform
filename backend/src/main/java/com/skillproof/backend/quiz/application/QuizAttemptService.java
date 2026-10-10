package com.skillproof.backend.quiz.application;

import com.skillproof.backend.quiz.infrastructure.persistence.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillproof.backend.common.exception.BadRequestException;
import com.skillproof.backend.common.exception.ConflictException;
import com.skillproof.backend.common.exception.NotFoundException;
import com.skillproof.backend.course.contract.CourseQuizAccess;
import com.skillproof.backend.quiz.contract.QuizCompletionEvidenceQuery;

@Service
public class QuizAttemptService {

    @jakarta.persistence.PersistenceContext
    private jakarta.persistence.EntityManager entityManager;

    private final QuizAssessmentRepository assessments;
    private final QuizAttemptRepository attempts;
    private final QuizAttemptQuestionRepository questions;
    private final QuizAssessmentQuestionRepository assessmentQuestions;
    private final QuizOptionRepository options;
    private final QuizQuestionRepository questionVersions;
    private final CourseQuizAccess learning;
    private final QuizCompletionEvidenceQuery evidence;

    public QuizAttemptService(QuizAssessmentRepository a, QuizAttemptRepository t, QuizAttemptQuestionRepository q,
            CourseQuizAccess l, QuizCompletionEvidenceQuery e, QuizAssessmentQuestionRepository aq,
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
        if (!learner.equals(a.getLearnerId())) {
            throw new NotFoundException("QUIZ_NOT_FOUND", "Quiz record not found");

        }
        return a;
    }

    private QuizAttempt attemptForUpdate(UUID learner, UUID id) {
        var snapshot = attempt(learner, id);
        learning.enrollment(learner, snapshot.getEnrollmentId(), true);
        var a = attempts.findForUpdate(id).orElseThrow(()
                -> new NotFoundException("QUIZ_NOT_FOUND", "Quiz record not found"));
        if (!learner.equals(a.getLearnerId())) {
            throw new NotFoundException("QUIZ_NOT_FOUND", "Quiz record not found");
        }
        entityManager.refresh(a, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
        return a;
    }

    public List<Map<String, Object>> forEnrollment(UUID learner, UUID enrollmentId) {
        var e = learning.enrollment(learner, enrollmentId, false);
        return assessments.findByVersionIdAndStatus(e.versionId(), QuizAssessment.Status.PUBLISHED).stream().map(a -> Map.<String, Object>of("id", a.getId(), "version_id", a.getVersionId(), "kind", a.getKind().name(), "title", a.getTitle(), "duration_seconds", a.getDurationSeconds(), "pass_percent", a.getPassPercent(), "max_attempts", a.getMaxAttempts())).toList();
    }

    public List<Map<String, Object>> attemptHistory(UUID learner, UUID enrollmentId) {
        learning.enrollment(learner, enrollmentId, false);
        return attempts.findByEnrollmentIdAndLearnerIdOrderByStartedAtDesc(enrollmentId, learner).stream().map(a -> {
            Map<String, Object> item = new java.util.LinkedHashMap<>();
            item.put("id", a.getId());
            item.put("assessment_id", a.getAssessmentId());
            item.put("status", a.getStatus().name());
            item.put("score_percent", a.getScorePercent());
            item.put("passed", a.getPassed());
            item.put("started_at", a.getStartedAt());
            item.put("deadline_at", a.getDeadlineAt());
            item.put("submitted_at", a.getSubmittedAt());
            return item;
        }).toList();
    }

    @Transactional
    public Map<String, Object> start(UUID learner, UUID assessmentId, UUID enrollmentId) {
        var e = learning.enrollment(learner, enrollmentId, true);
        var a = assessments.findById(assessmentId).orElseThrow(() -> new NotFoundException("ASSESSMENT_UNAVAILABLE", "Assessment is not available in this enrollment"));
        if (a.getStatus() != QuizAssessment.Status.PUBLISHED || !a.getVersionId().equals(e.versionId())) {
            throw new NotFoundException("ASSESSMENT_UNAVAILABLE", "Assessment is not available in this enrollment");
        }
        var active = attempts.findByAssessmentIdAndEnrollmentIdAndStatus(assessmentId, enrollmentId, QuizAttempt.AttemptStatus.IN_PROGRESS).stream().findFirst();
        if (active.isPresent()) {
            var existing = attemptForUpdate(learner, active.get().getId());
            if (existing.getDeadlineAt().isAfter(Instant.now())) {
                return view(existing);
            }
            finalizeAttempt(existing, learner);
        }
        if (attempts.countByAssessmentIdAndEnrollmentId(assessmentId, enrollmentId) >= a.getMaxAttempts()) {
            throw new ConflictException("ATTEMPT_LIMIT", "No attempts remaining");
        }
        var t = new QuizAttempt();
        t.setId(UUID.randomUUID());
        t.setAssessmentId(assessmentId);
        t.setEnrollmentId(enrollmentId);
        t.setLearnerId(learner);
        t.setStatus(QuizAttempt.AttemptStatus.IN_PROGRESS);
        t.setStartedAt(Instant.now());
        t.setDeadlineAt(t.getStartedAt().plusSeconds(a.getDurationSeconds()));
        attempts.save(t);
        for (var aq : assessmentQuestions.findByAssessmentIdOrderByPosition(assessmentId)) {
            var x = new QuizAttemptQuestion();
            x.setId(new QuizAttemptQuestionId(t.getId(), aq.getQuestionVersion().getId()));
            x.setPosition(aq.getPosition());
            x.setPoints(aq.getPoints());
            questions.save(x);
        }
        return view(t);
    }

    private Map<String, Object> view(QuizAttempt t) {
        var items = questions.findByIdAttemptIdOrderByPosition(t.getId()).stream().map(x -> {
            var q = questionVersions.findById(x.getId().getQuestionVersionId()).orElseThrow();
            var item = new java.util.LinkedHashMap<String, Object>();
            item.put("question_version_id", q.getId());
            item.put("position", x.getPosition());
            item.put("points", x.getPoints());
            item.put("selected_option_id", x.getSelectedOptionId());
            item.put("stem", q.getStem());
            item.put("options", options.findByQuestionVersionIdOrderByPosition(q.getId()).stream()
                    .map(o -> Map.<String, Object>of("id", o.getId(), "position", o.getPosition(), "body", o.getBody())).toList());
            return item;
        }).toList();
        return Map.of("attempt", Map.of("id", t.getId(), "assessment_id", t.getAssessmentId(), "status", t.getStatus().name(), "started_at", t.getStartedAt(), "deadline_at", t.getDeadlineAt()), "questions", items);
    }

    @Transactional
    public Map<String, Object> answer(UUID learner, UUID id, UUID question, UUID option) {
        var t = attemptForUpdate(learner, id);
        if (t.getStatus() != QuizAttempt.AttemptStatus.IN_PROGRESS) {
            throw new ConflictException("ATTEMPT_FINAL", "Attempt already finalized");

        }
        if (!t.getDeadlineAt().isAfter(Instant.now())) {
            return finalizeAttempt(t, learner);

        }
        var x = questions.findById(new QuizAttemptQuestionId(id, question)).orElseThrow(() -> new BadRequestException("ANSWER_INVALID", "Question or option does not belong to this attempt"));
        if (options.findByQuestionVersionIdOrderByPosition(question).stream().noneMatch(o -> o.getId().equals(option))) {
            throw new BadRequestException("ANSWER_INVALID", "Question or option does not belong to this attempt");

        }
        x.setSelectedOptionId(option);
        questions.save(x);
        return view(t);
    }

    @Transactional
    public Map<String, Object> submit(UUID learner, UUID id) {
        var t = attemptForUpdate(learner, id);
        if (t.getStatus() == QuizAttempt.AttemptStatus.SCORED
                || t.getStatus() == QuizAttempt.AttemptStatus.TIMED_OUT) {
            return result(learner, id);
        }
        return finalizeAttempt(t, learner);
    }

    private Map<String, Object> finalizeAttempt(QuizAttempt t, UUID learner) {
        boolean timedOut = !t.getDeadlineAt().isAfter(Instant.now());
        int total = 0, earned = 0;
        for (var x : questions.findByIdAttemptIdOrderByPosition(t.getId())) {
            total += x.getPoints();
            if (x.getSelectedOptionId() != null && options.findById(x.getSelectedOptionId()).map(o -> o.getCorrect()).orElse(false)) {
                earned += x.getPoints();
            }
        }
        var a = assessments.findById(t.getAssessmentId()).orElseThrow();
        t.setStatus(timedOut ? QuizAttempt.AttemptStatus.TIMED_OUT : QuizAttempt.AttemptStatus.SCORED);
        t.setSubmittedAt(Instant.now());
        t.setScorePercent(total == 0 ? 0 : earned * 100 / total);
        t.setPassed(t.getScorePercent() >= a.getPassPercent());
        attempts.saveAndFlush(t);
        if (t.getPassed() && a.getKind() == QuizAssessment.Kind.OFFICIAL) {
            var ev = evidence.evidence(a.getVersionId(), t.getEnrollmentId());
            learning.evaluateCompletion(t.getEnrollmentId(), ev.requiredOfficialAssessments(), ev.passedOfficialAssessments());
        }
        return result(learner, t.getId());
    }

    public Map<String, Object> result(UUID learner, UUID id) {
        var t = attempt(learner, id);
        if (t.getStatus() != QuizAttempt.AttemptStatus.SCORED
                && t.getStatus() != QuizAttempt.AttemptStatus.TIMED_OUT) {
            throw new ConflictException("RESULT_NOT_READY", "Submit the attempt to see its result");

        }
        return Map.of("attemptId", t.getId(), "assessmentId", t.getAssessmentId(), "scorePercent", t.getScorePercent(), "passed", t.getPassed(), "timedOut", t.getStatus() == QuizAttempt.AttemptStatus.TIMED_OUT);
    }

    @Transactional
    public Map<String, Object> resume(UUID learner, UUID id) {
        var t = attemptForUpdate(learner, id);
        if (t.getStatus() == QuizAttempt.AttemptStatus.IN_PROGRESS && !t.getDeadlineAt().isAfter(Instant.now())) {
            return finalizeAttempt(t, learner);
        }
        return t.getStatus() == QuizAttempt.AttemptStatus.SCORED
                || t.getStatus() == QuizAttempt.AttemptStatus.TIMED_OUT
                ? result(learner, id) : view(t);
    }
}
