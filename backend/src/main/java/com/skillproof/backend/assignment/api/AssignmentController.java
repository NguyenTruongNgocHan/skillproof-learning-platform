package com.skillproof.backend.assignment.api;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.skillproof.backend.assignment.application.AssignmentAuthoringService;
import com.skillproof.backend.assignment.application.AssignmentSubmissionService;
import com.skillproof.backend.assignment.infrastructure.persistence.AssignmentEntity;
import com.skillproof.backend.assignment.infrastructure.persistence.AssignmentSubmissionEntity;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/v1/assignments")
public class AssignmentController {

    private final AssignmentAuthoringService authoring;
    private final AssignmentSubmissionService submissions;

    public AssignmentController(AssignmentAuthoringService authoring, AssignmentSubmissionService submissions) {
        this.authoring = authoring;
        this.submissions = submissions;
    }

    public record AssignmentInput(@NotBlank
            @Pattern(regexp = "COURSE|MODULE|LESSON") String ownerScope, @NotNull UUID ownerId,
            @NotBlank
            @Size(max = 180) String title, @NotBlank
            @Size(max = 20000) String instructions, boolean required,
            @Min(1)
            @Max(100) int passPercent, @Min(1)
            @Max(20) int maxSubmissions, Instant dueAt) {

    }

    public record SubmissionInput(@NotNull UUID enrollmentId, @Size(max = 20000) String body, @Size(max = 1000) String link) {

    }

    public record GradeInput(@Min(0)
            @Max(100) int scorePercent, @NotBlank
            @Size(max = 2000) String feedback) {

    }

    @PostMapping("/versions/{version}")
    public AssignmentEntity create(Authentication auth, @PathVariable UUID version, @Valid @RequestBody AssignmentInput i) {
        return authoring.create((UUID) auth.getPrincipal(), version, i.ownerScope(), i.ownerId(), i.title(), i.instructions(), i.required(), i.passPercent(), i.maxSubmissions(), i.dueAt());
    }

    @GetMapping("/versions/{version}")
    public List<AssignmentEntity> authorList(Authentication auth, @PathVariable UUID version) {
        return authoring.authorList((UUID) auth.getPrincipal(), version);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
    public void delete(Authentication auth, @PathVariable UUID id) {
        authoring.delete((UUID) auth.getPrincipal(), id);
    }

    @GetMapping("/enrollments/{enrollment}")
    public List<AssignmentEntity> available(Authentication auth, @PathVariable UUID enrollment) {
        return submissions.forEnrollment((UUID) auth.getPrincipal(), enrollment);
    }

    @PostMapping("/{id}/submissions")
    public AssignmentSubmissionEntity prepare(Authentication auth, @PathVariable UUID id, @Valid @RequestBody SubmissionInput i) {
        return submissions.prepare((UUID) auth.getPrincipal(), id, i.enrollmentId(), i.body(), i.link());
    }

    public record DraftInput(@Size(max = 20000) String body, @Size(max = 1000) String link) {

    }

    @PutMapping("/submissions/{id}")
    public AssignmentSubmissionEntity edit(Authentication auth, @PathVariable UUID id, @Valid @RequestBody DraftInput i) {
        return submissions.editDraft((UUID) auth.getPrincipal(), id, i.body(), i.link());
    }

    @PostMapping("/submissions/{id}/submit")
    public AssignmentSubmissionEntity submit(Authentication auth, @PathVariable UUID id) {
        return submissions.submit((UUID) auth.getPrincipal(), id);
    }

    @PostMapping("/submissions/{id}/grade")
    public AssignmentSubmissionEntity grade(Authentication auth, @PathVariable UUID id, @Valid @RequestBody GradeInput i) {
        return submissions.grade((UUID) auth.getPrincipal(), id, i.scorePercent(), i.feedback());
    }

    @GetMapping("/{id}/submissions")
    public List<AssignmentSubmissionEntity> authored(Authentication auth, @PathVariable UUID id) {
        return submissions.authorSubmissions((UUID) auth.getPrincipal(), id);
    }

    @GetMapping("/{id}/enrollments/{enrollment}/submissions")
    public List<AssignmentSubmissionEntity> mine(Authentication auth, @PathVariable UUID id, @PathVariable UUID enrollment) {
        return submissions.mine((UUID) auth.getPrincipal(), id, enrollment);
    }
}
