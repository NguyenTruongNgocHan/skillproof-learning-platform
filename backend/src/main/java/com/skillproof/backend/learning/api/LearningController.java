package com.skillproof.backend.learning.api;

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

import com.skillproof.backend.learning.application.CompletionEvidenceService;
import com.skillproof.backend.learning.application.LearningProgressService;
import com.skillproof.backend.learning.application.LearningService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/v1/learning")
public class LearningController {

    private final LearningService service;

    private final LearningProgressService progress;

    private final CompletionEvidenceService completion;

    public LearningController(LearningService service, LearningProgressService progress, CompletionEvidenceService completion) {
        this.service = service;
        this.progress = progress;
        this.completion = completion;
    }

    private UUID user(Authentication auth) {
        return (UUID) auth.getPrincipal();
    }

    public record PathInput(@NotBlank
            @Pattern(regexp = "[a-z0-9]+(?:-[a-z0-9]+)*")
            @Size(max = 100) String slug,
            @NotBlank
            @Size(max = 180) String title, @NotBlank
            @Size(max = 1000) String summary) {

    }

    public record PathEdit(@NotBlank
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

    @GetMapping("/paths")
    public List<Map<String, Object>> discover() {
        return progress.discover();
    }

    @GetMapping("/paths/{id}")
    public Map<String, Object> detail(@PathVariable UUID id) {
        return progress.publicDetail(id);
    }

    @GetMapping("/organizations/{org}/paths")
    public List<Map<String, Object>> owned(Authentication auth, @PathVariable UUID org) {
        return service.owned(user(auth), org);
    }

    @PostMapping("/organizations/{org}/paths")
    @ResponseStatus(HttpStatus.CREATED)

    public Map<String, Object> create(Authentication auth, @PathVariable UUID org, @Valid @RequestBody PathInput input) {
        return service.create(user(auth), org, input.slug(), input.title(), input.summary());
    }

    @PatchMapping("/paths/{id}")
    public Map<String, Object> edit(Authentication auth, @PathVariable UUID id, @Valid @RequestBody PathEdit input) {
        return service.updatePath(user(auth), id, input.title(), input.summary());
    }

    @GetMapping("/paths/{id}/versions")
    public List<Map<String, Object>> versions(Authentication auth, @PathVariable UUID id) {
        return service.versions(user(auth), id);
    }

    @PostMapping("/paths/{id}/versions")
    public Map<String, Object> clone(Authentication auth, @PathVariable UUID id) {
        return service.cloneVersion(user(auth), id);
    }

    @GetMapping("/versions/{id}/outline")
    public Map<String, Object> outline(Authentication auth, @PathVariable UUID id) {
        return service.outline(user(auth), id);
    }

    @PostMapping("/versions/{id}/modules")
    public Map<String, Object> module(Authentication auth, @PathVariable UUID id, @Valid @RequestBody ModuleInput input) {
        return service.module(user(auth), id, input.position(), input.title());
    }

    @PatchMapping("/modules/{id}")
    public Map<String, Object> editModule(Authentication auth, @PathVariable UUID id, @Valid @RequestBody ModuleInput input) {
        return service.updateModule(user(auth), id, input.position(), input.title());
    }

    @DeleteMapping("/modules/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)

    public void deleteModule(Authentication auth, @PathVariable UUID id) {
        service.deleteModule(user(auth), id);
    }

    @PostMapping("/modules/{id}/resources")
    public Map<String, Object> resource(Authentication auth, @PathVariable UUID id, @Valid @RequestBody ResourceInput input) {
        return service.resource(user(auth), id, input.position(), input.kind(), input.title(), input.body(), input.url());
    }

    @PatchMapping("/resources/{id}")
    public Map<String, Object> editResource(Authentication auth, @PathVariable UUID id, @Valid @RequestBody ResourceInput input) {
        return service.updateResource(user(auth), id, input.position(), input.kind(), input.title(), input.body(), input.url());
    }

    @DeleteMapping("/resources/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)

    public void deleteResource(Authentication auth, @PathVariable UUID id) {
        service.deleteResource(user(auth), id);
    }

    @PostMapping("/versions/{id}/publish")
    public Map<String, Object> publish(Authentication auth, @PathVariable UUID id) {
        return service.publish(user(auth), id);
    }

    @PutMapping("/versions/{id}/policy")
    public Map<String, Object> policy(Authentication auth, @PathVariable UUID id, @Valid @RequestBody PolicyInput input) {
        return service.policy(user(auth), id, input.requireAllResources(), input.requireOfficialAssessments());
    }

    @PostMapping("/paths/{id}/enroll")
    @ResponseStatus(HttpStatus.CREATED)

    public Map<String, Object> enroll(Authentication auth, @PathVariable UUID id) {
        return progress.enroll(user(auth), id);
    }

    @GetMapping("/me/enrollments")
    public List<Map<String, Object>> mine(Authentication auth) {
        return progress.mine(user(auth));
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

}
