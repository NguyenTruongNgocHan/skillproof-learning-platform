package com.skillproof.backend.quiz.application;

import com.skillproof.backend.common.exception.*;
import com.skillproof.backend.learning.contract.LearningQuizAccess;
import java.time.Instant;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QuizService {

    private final QuizBankRepository banks;
    private final QuizQuestionRootRepository questions;
    private final QuizQuestionVersionRepository versions;
    private final QuizOptionRepository optionRepository;
    private final LearningQuizAccess access;

    public QuizService(QuizBankRepository banks, QuizQuestionRootRepository questions,
            QuizQuestionVersionRepository versions, QuizOptionRepository optionRepository, LearningQuizAccess access) {
        this.banks = banks;
        this.questions = questions;
        this.versions = versions;
        this.optionRepository = optionRepository;
        this.access = access;
    }

    private QuizBank bank(UUID id) {
        return banks.findById(id).orElseThrow(() -> new NotFoundException("QUIZ_NOT_FOUND", "Quiz record not found"));
    }

    private Map<String, Object> bankView(QuizBank b) {
        return Map.of("id", b.id, "organization_id", b.organizationId, "title", b.title, "created_by", b.createdBy, "created_at", b.createdAt);
    }

    public List<Map<String, Object>> banks(UUID actor, UUID org) {
        access.requireOrganizer(actor, org);
        return banks.findByOrganizationIdOrderByCreatedAtDesc(org).stream().map(this::bankView).toList();
    }

    @Transactional
    public Map<String, Object> createBank(UUID actor, UUID org, String title) {
        access.requireOrganizer(actor, org);
        var b = new QuizBank();
        b.id = UUID.randomUUID();
        b.organizationId = org;
        b.title = title.trim();
        b.createdBy = actor;
        b.createdAt = Instant.now();
        return bankView(banks.save(b));
    }

    public List<Map<String, Object>> questions(UUID actor, UUID bankId) {
        var b = bank(bankId);
        access.requireOrganizer(actor, b.organizationId);
        return questions.findByBankIdOrderByCreatedAtDesc(bankId).stream().map(q -> {
            var v = versions.findFirstByQuestionIdOrderByVersionNoDesc(q.id).orElseThrow();
            return Map.<String, Object>of("id", q.id, "version_id", v.id, "version_no", v.versionNo, "stem", v.stem);
        }).toList();
    }

    public Map<String, Object> question(UUID actor, UUID id) {
        var q = questions.findById(id).orElseThrow(() -> new NotFoundException("QUIZ_NOT_FOUND", "Quiz record not found"));
        var b = bank(q.bankId);
        access.requireOrganizer(actor, b.organizationId);
        var v = versions.findFirstByQuestionIdOrderByVersionNoDesc(id).orElseThrow();
        var qm = Map.<String, Object>of("id", q.id, "bank_id", q.bankId, "organization_id", b.organizationId);
        var vm = Map.<String, Object>of("id", v.id, "question_id", v.questionId, "version_no", v.versionNo, "stem", v.stem, "created_at", v.createdAt);
        var opts = optionRepository.findByQuestionVersionIdOrderByPosition(v.id).stream().map(o -> Map.<String, Object>of("id", o.id, "question_version_id", v.id, "position", o.position, "body", o.body, "correct", o.correct)).toList();
        return Map.of("question", qm, "version", vm, "options", opts);
    }

    public static void validateOptions(List<Option> options) {
        if (options == null || options.size() < 2 || options.size() > 8 || options.stream().anyMatch(Objects::isNull) || options.stream().filter(Option::correct).count() != 1) {
            throw new BadRequestException("QUESTION_OPTIONS_INVALID", "Provide 2–8 choices with exactly one correct answer");
        }
        for (var o : options) {
            if (o.body() == null || o.body().isBlank() || o.body().length() > 1000) {
                throw new BadRequestException("QUESTION_OPTIONS_INVALID", "Choice text must be 1–1000 characters");
            }
        }
    }

    public record Option(String body, boolean correct) {

    }

    @Transactional
    public Map<String, Object> addQuestion(UUID actor, UUID bankId, String stem, List<Option> options) {
        validateOptions(options);
        var b = bank(bankId);
        access.requireOrganizer(actor, b.organizationId);
        var now = Instant.now();
        var q = new QuizQuestion();
        q.id = UUID.randomUUID();
        q.bankId = bankId;
        q.createdAt = now;
        questions.save(q);
        var v = new QuizQuestionVersion();
        v.id = UUID.randomUUID();
        v.questionId = q.id;
        v.versionNo = 1;
        v.stem = stem.trim();
        v.createdAt = now;
        var newOptions = new ArrayList<QuizOption>();
        int pos = 1;
        for (var o : options) {
            var x = new QuizOption();
            x.id = UUID.randomUUID();
            x.questionVersionId = v.id;
            x.body = o.body().trim();
            x.correct = o.correct();
            x.position = pos++;
            newOptions.add(x);
        }
        versions.save(v);
        optionRepository.saveAll(newOptions);
        return question(actor, q.id);
    }

    @Transactional
    public Map<String, Object> revise(UUID actor, UUID questionId, String stem, List<Option> options) {
        validateOptions(options);
        var q = questions.findById(questionId).orElseThrow(() -> new NotFoundException("QUIZ_NOT_FOUND", "Quiz record not found"));
        var b = bank(q.bankId);
        access.requireOrganizer(actor, b.organizationId);
        var v = new QuizQuestionVersion();
        v.id = UUID.randomUUID();
        v.questionId = q.id;
        v.versionNo = Optional.ofNullable(versions.maxVersion(q.id)).orElse(0) + 1;
        v.stem = stem.trim();
        v.createdAt = Instant.now();
        var newOptions = new ArrayList<QuizOption>();
        int pos = 1;
        for (var o : options) {
            var x = new QuizOption();
            x.id = UUID.randomUUID();
            x.questionVersionId = v.id;
            x.body = o.body().trim();
            x.correct = o.correct();
            x.position = pos++;
            newOptions.add(x);
        }
        versions.save(v);
        optionRepository.saveAll(newOptions);
        return question(actor, questionId);
    }
}
