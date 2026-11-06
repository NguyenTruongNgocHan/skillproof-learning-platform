package com.skillproof.backend.assignment.api;

import com.skillproof.backend.assignment.api.model.AssignmentRubricCriterionResponse;
import com.skillproof.backend.assignment.api.model.AssignmentSubmissionResponse;
import com.skillproof.backend.assignment.api.model.SubmissionCriterionGradeResponse;
import com.skillproof.backend.assignment.application.*;
import com.skillproof.backend.assignment.infrastructure.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/assignments")
public class AssignmentRubricController {
  private final AssignmentRubricService rubric;
  private final AssignmentSubmissionService submissions;

  public AssignmentRubricController(
      AssignmentRubricService rubric, AssignmentSubmissionService submissions) {
    this.rubric = rubric;
    this.submissions = submissions;
  }

  public record RubricInput(
      @NotNull @Size(max = 20) List<AssignmentRubricService.CriterionInput> criteria) {}

  public record GradeInput(
      @NotEmpty @Size(max = 20) List<AssignmentSubmissionService.CriterionScore> criteria,
      @NotBlank @Size(max = 2000) String feedback) {}

  @GetMapping("/{id}/rubric")
  public List<AssignmentRubricCriterionResponse> list(Authentication a, @PathVariable UUID id) {
    return rubric.author((UUID) a.getPrincipal(), id).stream()
        .map(AssignmentRubricCriterionResponse::from)
        .toList();
  }

  @PutMapping("/{id}/rubric")
  public List<AssignmentRubricCriterionResponse> save(
      Authentication a, @PathVariable UUID id, @Valid @RequestBody RubricInput i) {
    return rubric.save((UUID) a.getPrincipal(), id, i.criteria()).stream()
        .map(AssignmentRubricCriterionResponse::from)
        .toList();
  }

  @GetMapping("/{id}/enrollments/{enrollment}/rubric")
  public List<AssignmentRubricCriterionResponse> learner(
      Authentication a, @PathVariable UUID id, @PathVariable UUID enrollment) {
    return rubric.learner((UUID) a.getPrincipal(), id, enrollment).stream()
        .map(AssignmentRubricCriterionResponse::from)
        .toList();
  }

  @PostMapping("/submissions/{id}/rubric-grade")
  public AssignmentSubmissionResponse grade(
      Authentication a, @PathVariable UUID id, @Valid @RequestBody GradeInput i) {
    return AssignmentSubmissionResponse.from(
        submissions.gradeRubric((UUID) a.getPrincipal(), id, i.criteria(), i.feedback()));
  }

  @GetMapping("/submissions/{id}/rubric-grade")
  public List<SubmissionCriterionGradeResponse> grades(Authentication a, @PathVariable UUID id) {
    return submissions.criterionGrades((UUID) a.getPrincipal(), id).stream()
        .map(SubmissionCriterionGradeResponse::from)
        .toList();
  }
}
