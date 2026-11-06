package com.skillproof.backend.assignment.api;

import com.skillproof.backend.assignment.api.model.AssignmentResponse;
import com.skillproof.backend.assignment.api.model.AssignmentSubmissionResponse;
import com.skillproof.backend.assignment.application.AssignmentAuthoringService;
import com.skillproof.backend.assignment.application.AssignmentSubmissionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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

@RestController
@RequestMapping("/api/v1/assignments")
public class AssignmentController {

  private final AssignmentAuthoringService authoring;
  private final AssignmentSubmissionService submissions;

  public AssignmentController(
      AssignmentAuthoringService authoring, AssignmentSubmissionService submissions) {
    this.authoring = authoring;
    this.submissions = submissions;
  }

  public record AssignmentInput(
      @NotBlank @Pattern(regexp = "COURSE|MODULE|LESSON") String ownerScope,
      @NotNull UUID ownerId,
      @NotBlank @Size(max = 180) String title,
      @NotBlank @Size(max = 20000) String instructions,
      boolean required,
      @Min(1) @Max(100) int passPercent,
      @Min(1) @Max(20) int maxSubmissions,
      Instant dueAt) {}

  public record SubmissionInput(
      @NotNull UUID enrollmentId, @Size(max = 20000) String body, @Size(max = 1000) String link) {}

  public record GradeInput(
      @Min(0) @Max(100) int scorePercent, @NotBlank @Size(max = 2000) String feedback) {}

  @PostMapping("/versions/{version}")
  public AssignmentResponse create(
      Authentication auth, @PathVariable UUID version, @Valid @RequestBody AssignmentInput i) {
    return AssignmentResponse.from(
        authoring.create(
            (UUID) auth.getPrincipal(),
            version,
            i.ownerScope(),
            i.ownerId(),
            i.title(),
            i.instructions(),
            i.required(),
            i.passPercent(),
            i.maxSubmissions(),
            i.dueAt()));
  }

  @GetMapping("/versions/{version}")
  public List<AssignmentResponse> authorList(Authentication auth, @PathVariable UUID version) {
    return authoring.authorList((UUID) auth.getPrincipal(), version).stream()
        .map(AssignmentResponse::from)
        .toList();
  }

  @PutMapping("/{id}")
  public AssignmentResponse edit(
      Authentication auth, @PathVariable UUID id, @Valid @RequestBody AssignmentInput i) {
    return AssignmentResponse.from(
        authoring.edit(
            (UUID) auth.getPrincipal(),
            id,
            i.ownerScope(),
            i.ownerId(),
            i.title(),
            i.instructions(),
            i.required(),
            i.passPercent(),
            i.maxSubmissions(),
            i.dueAt()));
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
  public void delete(Authentication auth, @PathVariable UUID id) {
    authoring.delete((UUID) auth.getPrincipal(), id);
  }

  @GetMapping("/enrollments/{enrollment}")
  public List<AssignmentResponse> available(Authentication auth, @PathVariable UUID enrollment) {
    return submissions.forEnrollment((UUID) auth.getPrincipal(), enrollment).stream()
        .map(AssignmentResponse::from)
        .toList();
  }

  @PostMapping("/{id}/submissions")
  public AssignmentSubmissionResponse prepare(
      Authentication auth, @PathVariable UUID id, @Valid @RequestBody SubmissionInput i) {
    return AssignmentSubmissionResponse.from(
        submissions.prepare((UUID) auth.getPrincipal(), id, i.enrollmentId(), i.body(), i.link()));
  }

  public record DraftInput(@Size(max = 20000) String body, @Size(max = 1000) String link) {}

  @PutMapping("/submissions/{id}")
  public AssignmentSubmissionResponse edit(
      Authentication auth, @PathVariable UUID id, @Valid @RequestBody DraftInput i) {
    return AssignmentSubmissionResponse.from(
        submissions.editDraft((UUID) auth.getPrincipal(), id, i.body(), i.link()));
  }

  @PostMapping("/submissions/{id}/submit")
  public AssignmentSubmissionResponse submit(Authentication auth, @PathVariable UUID id) {
    return AssignmentSubmissionResponse.from(submissions.submit((UUID) auth.getPrincipal(), id));
  }

  @PostMapping("/submissions/{id}/grade")
  public AssignmentSubmissionResponse grade(
      Authentication auth, @PathVariable UUID id, @Valid @RequestBody GradeInput i) {
    return AssignmentSubmissionResponse.from(
        submissions.grade((UUID) auth.getPrincipal(), id, i.scorePercent(), i.feedback()));
  }

  @GetMapping("/{id}/submissions")
  public List<AssignmentSubmissionResponse> authored(Authentication auth, @PathVariable UUID id) {
    return submissions.authorSubmissions((UUID) auth.getPrincipal(), id).stream()
        .map(AssignmentSubmissionResponse::from)
        .toList();
  }

  @GetMapping("/{id}/enrollments/{enrollment}/submissions")
  public List<AssignmentSubmissionResponse> mine(
      Authentication auth, @PathVariable UUID id, @PathVariable UUID enrollment) {
    return submissions.mine((UUID) auth.getPrincipal(), id, enrollment).stream()
        .map(AssignmentSubmissionResponse::from)
        .toList();
  }
}
