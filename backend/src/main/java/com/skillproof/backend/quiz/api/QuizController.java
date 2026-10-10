package com.skillproof.backend.quiz.api;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.skillproof.backend.quiz.application.AssessmentAuthoringService;
import com.skillproof.backend.quiz.application.QuizAttemptService;
import com.skillproof.backend.quiz.application.QuizAuthoringService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/v1/quiz")
public class QuizController {

    private final QuizAuthoringService service;

    private final QuizAttemptService attempts;

    private final AssessmentAuthoringService authoring;

    public QuizController(QuizAuthoringService service, QuizAttemptService attempts, AssessmentAuthoringService authoring) {
        this.service = service;
        this.attempts = attempts;
        this.authoring = authoring;
    }

    private UUID user(Authentication auth) {
        return (UUID) auth.getPrincipal();
    }

    public record Title(@NotBlank
            @Size(max = 180) String title) {

    }

    public record QuestionInput(@NotBlank
            @Size(max = 2000) String stem, @NotNull
            @Size(min = 2, max = 8) List<QuizAuthoringService.Option> options) {

    }

    public record AssessmentInput(@NotBlank
            @Pattern(regexp = "PRACTICE|MOCK|OFFICIAL") String kind,
            @NotBlank
            @Size(max = 180) String title, @Min(60)
            @Max(14400) int durationSeconds,
            @Min(1)
            @Max(100) int passPercent, @Min(1)
            @Max(20) int maxAttempts) {

    }

    public record AttachInput(@NotNull UUID questionVersionId, @Min(1) int position, @Min(1)
            @Max(100) int points) {

    }

    public record AssessmentEdit(@NotBlank
            @Size(max = 180) String title, @Min(60)
            @Max(14400) int durationSeconds,
            @Min(1)
            @Max(100) int passPercent, @Min(1)
            @Max(20) int maxAttempts) {

    }

    public record StartInput(@NotNull UUID enrollmentId) {

    }

    public record AnswerInput(@NotNull UUID questionVersionId, @NotNull UUID optionId) {

    }

    @GetMapping("/organizations/{org}/banks")
    public List<com.skillproof.backend.quiz.application.model.BankView> banks(Authentication auth, @PathVariable UUID org) {
        return service.banks(user(auth), org);
    }

    @PostMapping("/organizations/{org}/banks")
    @ResponseStatus(HttpStatus.CREATED)

    public com.skillproof.backend.quiz.application.model.BankView bank(Authentication auth, @PathVariable UUID org, @Valid @RequestBody Title input) {
        return service.createBank(user(auth), org, input.title());
    }

    @GetMapping("/banks/{id}/questions")
    public List<Map<String, Object>> questions(Authentication auth, @PathVariable UUID id) {
        return service.questions(user(auth), id);
    }

    @PostMapping("/banks/{id}/questions")
    @ResponseStatus(HttpStatus.CREATED)

    public Map<String, Object> question(Authentication auth, @PathVariable UUID id, @Valid @RequestBody QuestionInput input) {
        return service.addQuestion(user(auth), id, input.stem(), input.options());
    }

    @GetMapping("/questions/{id}")
    public Map<String, Object> question(Authentication auth, @PathVariable UUID id) {
        return service.question(user(auth), id);
    }

    @PostMapping("/questions/{id}/versions")
    public Map<String, Object> revise(Authentication auth, @PathVariable UUID id, @Valid @RequestBody QuestionInput input) {
        return service.revise(user(auth), id, input.stem(), input.options());
    }

    @GetMapping("/versions/{id}/assessments")
    public List<Map<String, Object>> assessments(Authentication auth, @PathVariable UUID id) {
        return authoring.assessments(user(auth), id);
    }

    @PostMapping("/versions/{id}/assessments")
    @ResponseStatus(HttpStatus.CREATED)

    public Map<String, Object> create(Authentication auth, @PathVariable UUID id, @Valid @RequestBody AssessmentInput input) {
        return authoring.createAssessment(user(auth), id, input.kind(), input.title(), input.durationSeconds(), input.passPercent(), input.maxAttempts());
    }

    @PatchMapping("/assessments/{id}")
    public Map<String, Object> edit(Authentication auth, @PathVariable UUID id, @Valid @RequestBody AssessmentEdit input) {
        return authoring.updateAssessment(user(auth), id, input.title(), input.durationSeconds(), input.passPercent(), input.maxAttempts());
    }

    @PostMapping("/assessments/{id}/questions")
    @ResponseStatus(HttpStatus.NO_CONTENT)

    public void attach(Authentication auth, @PathVariable UUID id, @Valid @RequestBody AttachInput input) {
        authoring.attach(user(auth), id, input.questionVersionId(), input.position(), input.points());
    }

    @GetMapping("/assessments/{id}/questions")
    public List<Map<String, Object>> assessmentQuestions(Authentication auth, @PathVariable UUID id) {
        return authoring.assessmentQuestions(user(auth), id);
    }

    @DeleteMapping("/assessments/{id}/questions/{questionVersion}")
    @ResponseStatus(HttpStatus.NO_CONTENT)

    public void detach(Authentication auth, @PathVariable UUID id, @PathVariable UUID questionVersion) {
        authoring.detach(user(auth), id, questionVersion);
    }

    @DeleteMapping("/assessments/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)

    public void delete(Authentication auth, @PathVariable UUID id) {
        authoring.deleteAssessment(user(auth), id);
    }

    @PostMapping("/assessments/{id}/publish")
    public Map<String, Object> publish(Authentication auth, @PathVariable UUID id) {
        return authoring.publish(user(auth), id);
    }

    @GetMapping("/me/enrollments/{enrollment}/assessments")
    public List<Map<String, Object>> available(Authentication auth, @PathVariable UUID enrollment) {
        return attempts.forEnrollment(user(auth), enrollment);
    }

    @GetMapping("/me/enrollments/{enrollment}/attempts")
    public List<Map<String, Object>> attemptHistory(Authentication auth, @PathVariable UUID enrollment) {
        return attempts.attemptHistory(user(auth), enrollment);
    }

    @PostMapping("/assessments/{id}/attempts")
    @ResponseStatus(HttpStatus.CREATED)

    public Map<String, Object> start(Authentication auth, @PathVariable UUID id, @Valid @RequestBody StartInput input) {
        return attempts.start(user(auth), id, input.enrollmentId());
    }

    @GetMapping("/attempts/{id}")
    public Map<String, Object> resume(Authentication auth, @PathVariable UUID id) {
        return attempts.resume(user(auth), id);
    }

    @PutMapping("/attempts/{id}/answers")
    public Map<String, Object> answer(Authentication auth, @PathVariable UUID id, @Valid @RequestBody AnswerInput input) {
        return attempts.answer(user(auth), id, input.questionVersionId(), input.optionId());
    }

    @PostMapping("/attempts/{id}/submit")
    public Map<String, Object> submit(Authentication auth, @PathVariable UUID id) {
        return attempts.submit(user(auth), id);
    }

    @GetMapping("/attempts/{id}/result")
    public Map<String, Object> result(Authentication auth, @PathVariable UUID id) {
        return attempts.result(user(auth), id);
    }

    @GetMapping("/me/banks")
    public List<com.skillproof.backend.quiz.application.model.BankView> personalBanks(Authentication auth) {
        return service.personalBanks(user(auth));
    }

    @PostMapping("/me/banks")
    public com.skillproof.backend.quiz.application.model.BankView personalBank(Authentication auth, @Valid @RequestBody Title input) {
        return service.createBank(user(auth), null, input.title());
    }

    public record ActivityOwner(@NotBlank
            @Pattern(regexp = "COURSE|MODULE|LESSON") String scope, @NotNull UUID ownerId, boolean required) {

    }

    @PutMapping("/assessments/{id}/owner")
    public Map<String, Object> owner(Authentication auth, @PathVariable UUID id, @Valid @RequestBody ActivityOwner input) {
        return authoring.configureOwner(user(auth), id, input.scope(), input.ownerId(), input.required());
    }

}
