package com.skillproof.backend.course.api;

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

import com.skillproof.backend.course.application.CompletionEvidenceService;
import com.skillproof.backend.course.application.CourseAuthoringService;
import com.skillproof.backend.course.application.CourseProgressService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/v1/courses")
public class CourseController {

    private final CourseAuthoringService service;
    private final com.skillproof.backend.course.application.CourseContentService content;
    private final com.skillproof.backend.course.application.CourseCatalogService catalog;
    private final com.skillproof.backend.course.application.CourseEnrollmentService enrollmentService;

    private final CourseProgressService progress;

    private final CompletionEvidenceService completion;

    public CourseController(CourseAuthoringService service, CourseProgressService progress, CompletionEvidenceService completion, com.skillproof.backend.course.application.CourseCatalogService catalog, com.skillproof.backend.course.application.CourseEnrollmentService enrollmentService, com.skillproof.backend.course.application.CourseContentService content) {
        this.service = service;
        this.content = content;
        this.progress = progress;
        this.completion = completion;
        this.catalog = catalog;
        this.enrollmentService = enrollmentService;
    }

    private UUID user(Authentication auth) {
        return (UUID) auth.getPrincipal();
    }

    public record CourseInput(@NotBlank
            @Pattern(regexp = "[a-z0-9]+(?:-[a-z0-9]+)*")
            @Size(max = 100) String slug,
            @NotBlank
            @Size(max = 180) String title, @NotBlank
            @Size(max = 1000) String summary) {

    }

    public record CourseEdit(@NotBlank
            @Size(max = 180) String title, @NotBlank
            @Size(max = 1000) String summary) {

    }

    public record ModuleInput(@Min(1) int position, @NotBlank
            @Size(max = 180) String title) {

    }

    public record ResourceInput(@Min(1) int position, @NotBlank String kind, @NotBlank
            @Size(max = 180) String title,
            String body, @Size(max = 1000) String url) {

    }

    public record PolicyInput(boolean requireAllResources, boolean requireOfficialAssessments) {

    }

    @GetMapping("")
    public List<com.skillproof.backend.course.application.CourseCatalogService.Summary> discover(@org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page, @org.springframework.web.bind.annotation.RequestParam(defaultValue = "20") int size) {
        return catalog.discover(page, size);
    }

    @GetMapping("/{id}")
    public com.skillproof.backend.course.application.CourseCatalogService.Summary detail(@PathVariable UUID id) {
        return catalog.publicDetail(id);
    }

    @GetMapping("/{id}/syllabus")
    public List<com.skillproof.backend.course.application.CourseCatalogService.ModuleSummary> syllabus(@PathVariable UUID id) {
        return catalog.syllabus(id);
    }

    @GetMapping("/organizations/{org}")
    public List<Map<String, Object>> owned(Authentication auth, @PathVariable UUID org) {
        return service.owned(user(auth), org);
    }

    @PostMapping("/organizations/{org}")
    @ResponseStatus(HttpStatus.CREATED)

    public Map<String, Object> create(Authentication auth, @PathVariable UUID org, @Valid @RequestBody CourseInput input) {
        return service.create(user(auth), org, input.slug(), input.title(), input.summary());
    }

    @PatchMapping("/{id}")
    public Map<String, Object> edit(Authentication auth, @PathVariable UUID id, @Valid @RequestBody CourseEdit input) {
        return service.updateCourse(user(auth), id, input.title(), input.summary());
    }

    @GetMapping("/{id}/versions")
    public List<Map<String, Object>> versions(Authentication auth, @PathVariable UUID id) {
        return service.versions(user(auth), id);
    }

    @PostMapping("/{id}/versions")
    public Map<String, Object> clone(Authentication auth, @PathVariable UUID id) {
        return service.cloneVersion(user(auth), id);
    }

    @GetMapping("/versions/{id}/outline")
    public Map<String, Object> outline(Authentication auth, @PathVariable UUID id) {
        return service.outline(user(auth), id);
    }

    @PostMapping("/versions/{id}/modules")
    public Map<String, Object> module(Authentication auth, @PathVariable UUID id, @Valid @RequestBody ModuleInput input) {
        return content.module(user(auth), id, input.position(), input.title());
    }

    @PatchMapping("/modules/{id}")
    public Map<String, Object> editModule(Authentication auth, @PathVariable UUID id, @Valid @RequestBody ModuleInput input) {
        return content.updateModule(user(auth), id, input.position(), input.title());
    }

    @DeleteMapping("/modules/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)

    public void deleteModule(Authentication auth, @PathVariable UUID id) {
        content.deleteModule(user(auth), id);
    }

    @PatchMapping("/resources/{id}")
    public Map<String, Object> editResource(Authentication auth, @PathVariable UUID id, @Valid @RequestBody ResourceInput input) {
        return content.updateResource(user(auth), id, input.position(), input.kind(), input.title(), input.body(), input.url());
    }

    @DeleteMapping("/resources/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)

    public void deleteResource(Authentication auth, @PathVariable UUID id) {
        content.deleteResource(user(auth), id);
    }

    @PostMapping("/versions/{id}/publish")
    public Map<String, Object> publish(Authentication auth, @PathVariable UUID id) {
        return service.publish(user(auth), id);
    }

    @PutMapping("/versions/{id}/policy")
    public Map<String, Object> policy(Authentication auth, @PathVariable UUID id, @Valid @RequestBody PolicyInput input) {
        return service.policy(user(auth), id, input.requireAllResources(), input.requireOfficialAssessments());
    }

    @PostMapping("/{id}/enroll")
    @ResponseStatus(HttpStatus.CREATED)

    public Map<String, Object> enroll(Authentication auth, @PathVariable UUID id) {
        return enrollmentService.enroll(user(auth), id);
    }

    @GetMapping("/me/enrollments")
    public List<Map<String, Object>> mine(Authentication auth) {
        return enrollmentService.mine(user(auth));
    }

    @GetMapping("/me/enrollments/{id}")
    public Map<String, Object> course(Authentication auth, @PathVariable UUID id) {
        return progress.course(user(auth), id);
    }

    @PostMapping("/me/enrollments/{id}/resources/{resource}/complete")
    public Map<String, Object> complete(Authentication auth, @PathVariable UUID id, @PathVariable UUID resource) {
        return progress.complete(user(auth), id, resource);
    }

    @PostMapping("/me/enrollments/{id}/completion/evaluate")
    public Object evaluateCompletion(Authentication auth, @PathVariable UUID id) {
        return completion.readForLearner(id, user(auth));
    }

    @PostMapping("/me/contributions")
    public Map<String, Object> contribute(Authentication auth, @Valid @RequestBody CourseInput input) {
        return service.create(user(auth), null, input.slug(), input.title(), input.summary());
    }

    @GetMapping("/me/contributions")
    public List<Map<String, Object>> contributions(Authentication auth) {
        return service.contributed(user(auth));
    }

    public record OfferInput(@Pattern(regexp = "PUBLIC|RESTRICTED")
            @NotBlank String accessMode,
            @jakarta.validation.constraints.PositiveOrZero long priceVnd, @jakarta.validation.constraints.PositiveOrZero long certificationPriceVnd) {

    }

    @PutMapping("/versions/{id}/offer")
    public Map<String, Object> offer(Authentication auth, @PathVariable UUID id, @Valid @RequestBody OfferInput input) {
        return service.configureOffer(user(auth), id, input.accessMode(), input.priceVnd(), input.certificationPriceVnd());
    }

    @PostMapping("/versions/{id}/submit-review")
    public Map<String, Object> submitReview(Authentication auth, @PathVariable UUID id) {
        return service.submitReview(user(auth), id);
    }

    @PostMapping("/versions/{id}/enroll")
    public Map<String, Object> enrollVersion(Authentication auth, @PathVariable UUID id) {
        return enrollmentService.enrollVersion(user(auth), id);
    }

}
