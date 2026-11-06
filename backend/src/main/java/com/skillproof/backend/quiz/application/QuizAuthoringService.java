package com.skillproof.backend.quiz.application;

import com.skillproof.backend.common.exception.BadRequestException;
import com.skillproof.backend.common.exception.NotFoundException;
import com.skillproof.backend.course.contract.CourseQuizAccess;
import com.skillproof.backend.quiz.infrastructure.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

/** Coordinates question-bank and question-version authoring use cases. */
@org.springframework.stereotype.Service
public class QuizAuthoringService {
  @org.springframework.beans.factory.annotation.Autowired
  private com.skillproof.backend.identity.contract.IdentityAccessQuery identity;

  private final QuizBankRepository banks;
  private final QuizQuestionRootRepository questions;
  private final QuizQuestionVersionRepository versions;
  private final QuizOptionRepository optionRepository;
  private final CourseQuizAccess access;

  public QuizAuthoringService(
      QuizBankRepository banks,
      QuizQuestionRootRepository questions,
      QuizQuestionVersionRepository versions,
      QuizOptionRepository optionRepository,
      CourseQuizAccess access) {
    this.banks = banks;
    this.questions = questions;
    this.versions = versions;
    this.optionRepository = optionRepository;
    this.access = access;
  }

  private QuizBank bank(UUID id) {
    return banks
        .findById(id)
        .orElseThrow(() -> new NotFoundException("QUIZ_NOT_FOUND", "Quiz record not found"));
  }

  private com.skillproof.backend.quiz.application.model.BankView bankView(QuizBank b) {
    return new com.skillproof.backend.quiz.application.model.BankView(
        b.getId(),
        b.getOrganizationId(),
        b.getTitle(),
        b.getCreatedBy(),
        b.getCreatedAt(),
        b.isArchived());
  }

  private void requireBankAuthor(UUID actor, QuizBank bank) {
    if (bank.getOrganizationId() == null) {
      access.requirePersonalAuthor(actor, bank.getCreatedBy());
    } else {
      access.requireOrganizer(actor, bank.getOrganizationId());
    }
  }

  public List<com.skillproof.backend.quiz.application.model.BankView> personalBanks(UUID actor) {
    access.requireLearner(actor);
    return banks.findByCreatedByAndOrganizationIdIsNullOrderByCreatedAtDesc(actor).stream()
        .map(this::bankView)
        .toList();
  }

  public List<com.skillproof.backend.quiz.application.model.BankView> banks(UUID actor, UUID org) {
    access.requireOrganizer(actor, org);
    return banks.findByOrganizationIdOrderByCreatedAtDesc(org).stream()
        .map(this::bankView)
        .toList();
  }

  @Transactional
  public com.skillproof.backend.quiz.application.model.BankView createBank(
      UUID actor, UUID org, String title) {
    if (org == null) {
      access.requireLearner(actor);
    } else {
      access.requireOrganizer(actor, org);
    }
    if (title == null || title.isBlank() || title.length() > 180)
      throw new BadRequestException(
          "BANK_TITLE", "Bank title is required and at most 180 characters");
    var b = new QuizBank();
    b.setId(UUID.randomUUID());
    b.setOrganizationId(org);
    b.setTitle(title.trim());
    b.setCreatedBy(actor);
    b.setCreatedAt(Instant.now());
    return bankView(banks.save(b));
  }

  public List<Map<String, Object>> questions(UUID actor, UUID bankId) {
    var b = bank(bankId);
    requireBankAuthor(actor, b);
    return questions.findByBankIdOrderByCreatedAtDesc(bankId).stream()
        .map(
            q -> {
              var v = versions.findFirstByQuestionIdOrderByVersionNoDesc(q.getId()).orElseThrow();
              return Map.<String, Object>of(
                  "id",
                  q.getId(),
                  "version_id",
                  v.getId(),
                  "version_no",
                  v.getVersionNo(),
                  "stem",
                  v.getStem(),
                  "archived",
                  q.isArchived());
            })
        .toList();
  }

  public Map<String, Object> question(UUID actor, UUID id) {
    var q =
        questions
            .findById(id)
            .orElseThrow(() -> new NotFoundException("QUIZ_NOT_FOUND", "Quiz record not found"));
    var b = bank(q.getBankId());
    requireBankAuthor(actor, b);
    var v = versions.findFirstByQuestionIdOrderByVersionNoDesc(id).orElseThrow();
    var qm =
        Map.<String, Object>of(
            "id", q.getId(),
            "bank_id", q.getBankId(),
            "bank_owner", b.getCreatedBy());
    var vm =
        Map.<String, Object>of(
            "id", v.getId(),
            "question_id", v.getQuestionId(),
            "version_no", v.getVersionNo(),
            "stem", v.getStem(),
            "created_at", v.getCreatedAt());
    var opts =
        optionRepository.findByQuestionVersionIdOrderByPosition(v.getId()).stream()
            .map(
                o ->
                    Map.<String, Object>of(
                        "id", o.getId(),
                        "question_version_id", v.getId(),
                        "position", o.getPosition(),
                        "body", o.getBody(),
                        "correct", o.getCorrect()))
            .toList();
    return Map.of("question", qm, "version", vm, "options", opts);
  }

  public static void validateOptions(List<Option> options) {
    try {
      com.skillproof.backend.quiz.domain.QuestionRules.validate(
          options == null
              ? null
              : options.stream()
                  .map(
                      o ->
                          o == null
                              ? null
                              : new com.skillproof.backend.quiz.domain.QuestionRules.Choice(
                                  o.body(), o.correct()))
                  .toList());
    } catch (IllegalArgumentException invalid) {
      throw new BadRequestException("QUESTION_OPTIONS_INVALID", invalid.getMessage());
    }
  }

  public record Option(String body, boolean correct) {}

  @Transactional
  public Map<String, Object> addQuestion(
      UUID actor, UUID bankId, String stem, List<Option> options) {
    validateOptions(options);
    if (stem == null || stem.isBlank() || stem.length() > 2000)
      throw new BadRequestException(
          "QUESTION_STEM", "Question text is required and at most 2000 characters");
    var b = bank(bankId);
    requireBankAuthor(actor, b);
    requireEditable(b, null);
    var now = Instant.now();
    var q = new QuizQuestion();
    q.setId(UUID.randomUUID());
    q.setBankId(bankId);
    q.setCreatedAt(now);
    questions.save(q);
    var v = new QuizQuestionVersion();
    v.setId(UUID.randomUUID());
    v.setQuestionId(q.getId());
    v.setVersionNo(1);
    v.setStem(stem.trim());
    v.setCreatedAt(now);
    var newOptions = new ArrayList<QuizOption>();
    int pos = 1;
    for (var o : options) {
      var x = new QuizOption();
      x.setId(UUID.randomUUID());
      x.setQuestionVersionId(v.getId());
      x.setBody(o.body().trim());
      x.setCorrect(o.correct());
      x.setPosition(pos++);
      newOptions.add(x);
    }
    versions.save(v);
    optionRepository.saveAll(newOptions);
    return question(actor, q.getId());
  }

  @Transactional
  public Map<String, Object> revise(
      UUID actor, UUID questionId, String stem, List<Option> options) {
    validateOptions(options);
    if (stem == null || stem.isBlank() || stem.length() > 2000)
      throw new BadRequestException(
          "QUESTION_STEM", "Question text is required and at most 2000 characters");
    var q =
        questions
            .findForUpdate(questionId)
            .orElseThrow(() -> new NotFoundException("QUIZ_NOT_FOUND", "Quiz record not found"));
    var b = bank(q.getBankId());
    requireBankAuthor(actor, b);
    requireEditable(b, q);
    var v = new QuizQuestionVersion();
    v.setId(UUID.randomUUID());
    v.setQuestionId(q.getId());
    v.setVersionNo(Optional.ofNullable(versions.maxVersion(q.getId())).orElse(0) + 1);
    v.setStem(stem.trim());
    v.setCreatedAt(Instant.now());
    var newOptions = new ArrayList<QuizOption>();
    int pos = 1;
    for (var o : options) {
      var x = new QuizOption();
      x.setId(UUID.randomUUID());
      x.setQuestionVersionId(v.getId());
      x.setBody(o.body().trim());
      x.setCorrect(o.correct());
      x.setPosition(pos++);
      newOptions.add(x);
    }
    versions.save(v);
    optionRepository.saveAll(newOptions);
    return question(actor, questionId);
  }

  @Transactional(readOnly = true)
  public Map<String, Object> versionPreview(UUID actor, UUID id) {
    var v = versions.findById(id).orElseThrow();
    var root = questions.findById(v.getQuestionId()).orElseThrow();
    if (!identity.isActiveAdmin(actor)) requireBankAuthor(actor, bank(root.getBankId()));
    return Map.of(
        "id",
        id,
        "versionNo",
        v.getVersionNo(),
        "stem",
        v.getStem(),
        "options",
        optionRepository.findByQuestionVersionIdOrderByPosition(id).stream()
            .map(o -> Map.of("id", o.getId(), "body", o.getBody(), "correct", o.getCorrect()))
            .toList());
  }

  private void requireEditable(QuizBank bank, QuizQuestion question) {
    if (bank.isArchived() || question != null && question.isArchived())
      throw new com.skillproof.backend.common.exception.ConflictException(
          "QUESTION_ARCHIVED", "Archived bank or question is read-only");
  }

  @Transactional
  public void archiveBank(UUID actor, UUID id) {
    var b =
        banks
            .findForUpdate(id)
            .orElseThrow(() -> new NotFoundException("BANK_NOT_FOUND", "Bank not found"));
    requireBankAuthor(actor, b);
    b.archive();
  }

  @Transactional
  public void archiveQuestion(UUID actor, UUID id) {
    var q =
        questions
            .findForUpdate(id)
            .orElseThrow(() -> new NotFoundException("QUESTION_NOT_FOUND", "Question not found"));
    var b = bank(q.getBankId());
    requireBankAuthor(actor, b);
    q.archive();
  }

  @Transactional(readOnly = true)
  public List<Map<String, Object>> revisions(UUID actor, UUID id) {
    var q =
        questions
            .findById(id)
            .orElseThrow(() -> new NotFoundException("QUESTION_NOT_FOUND", "Question not found"));
    requireBankAuthor(actor, bank(q.getBankId()));
    return versions.findByQuestionIdOrderByVersionNoDesc(id).stream()
        .map(
            v ->
                Map.<String, Object>of(
                    "id",
                    v.getId(),
                    "questionId",
                    id,
                    "versionNo",
                    v.getVersionNo(),
                    "stem",
                    v.getStem(),
                    "createdAt",
                    v.getCreatedAt()))
        .toList();
  }

  @Transactional
  public com.skillproof.backend.quiz.application.model.BankView renameBank(
      UUID actor, UUID id, String title) {
    var b =
        banks
            .findForUpdate(id)
            .orElseThrow(() -> new NotFoundException("BANK_NOT_FOUND", "Bank not found"));
    requireBankAuthor(actor, b);
    requireEditable(b, null);
    if (title == null || title.isBlank() || title.length() > 180)
      throw new BadRequestException(
          "BANK_TITLE", "Bank title is required and at most 180 characters");
    b.setTitle(title.trim());
    return bankView(b);
  }
}
