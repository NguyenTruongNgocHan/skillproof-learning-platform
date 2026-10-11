package com.skillproof.backend.assignment.api;

import com.skillproof.backend.assignment.application.*;
import com.skillproof.backend.assignment.infrastructure.persistence.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

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
    public List<AssignmentRubricCriterion> list(Authentication a, @PathVariable UUID id) {
        return rubric.author((UUID) a.getPrincipal(), id);
    }

    @PutMapping("/{id}/rubric")
    public List<AssignmentRubricCriterion> save(
            Authentication a, @PathVariable UUID id, @Valid @RequestBody RubricInput i) {
        return rubric.save((UUID) a.getPrincipal(), id, i.criteria());
    }

    @GetMapping("/{id}/enrollments/{enrollment}/rubric")
    public List<AssignmentRubricCriterion> learner(
            Authentication a, @PathVariable UUID id, @PathVariable UUID enrollment) {
        return rubric.learner((UUID) a.getPrincipal(), id, enrollment);
    }

    @PostMapping("/submissions/{id}/rubric-grade")
    public AssignmentSubmissionEntity grade(
            Authentication a, @PathVariable UUID id, @Valid @RequestBody GradeInput i) {
        return submissions.gradeRubric((UUID) a.getPrincipal(), id, i.criteria(), i.feedback());
    }

    @GetMapping("/submissions/{id}/rubric-grade")
    public List<SubmissionCriterionGrade> grades(Authentication a, @PathVariable UUID id) {
        return submissions.criterionGrades((UUID) a.getPrincipal(), id);
    }
}
