package com.skillproof.backend.assignment.application;

import com.skillproof.backend.assignment.contract.LearnerAssignmentHistoryQuery;
import com.skillproof.backend.assignment.infrastructure.persistence.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LearnerAssignmentHistoryService implements LearnerAssignmentHistoryQuery {
  private final AssignmentRubricRepository rubric;
  private final AssignmentRepository assignments;
  private final AssignmentSubmissionRepository submissions;
  private final SubmissionCriterionGradeRepository grades;

  public LearnerAssignmentHistoryService(
      AssignmentRepository assignments,
      AssignmentSubmissionRepository submissions,
      SubmissionCriterionGradeRepository grades,
      AssignmentRubricRepository rubric) {
    this.rubric = rubric;
    this.assignments = assignments;
    this.submissions = submissions;
    this.grades = grades;
  }

  @Transactional(readOnly = true)
  public List<Map<String, Object>> history(UUID learner, UUID enrollment, UUID version) {
    return assignments.findByVersionId(version).stream()
        .map(
            a -> {
              Map<String, Object> row = new LinkedHashMap<>();
              row.put("id", a.getId());
              row.put("title", a.getTitle());
              row.put("required", a.getRequired());
              row.put("passPercent", a.getPassPercent());
              row.put("maxSubmissions", a.getMaxSubmissions());
              row.put(
                  "submissions",
                  submissions
                      .findByAssignmentIdAndEnrollmentIdOrderByCreatedAtDesc(a.getId(), enrollment)
                      .stream()
                      .filter(
                          s -> learner.equals(s.getLearnerId()) && !"DRAFT".equals(s.getStatus()))
                      .map(
                          s -> {
                            Map<String, Object> item = new LinkedHashMap<>();
                            item.put("id", s.getId());
                            item.put("status", s.getStatus());
                            item.put("scorePercent", s.getScorePercent());
                            item.put("passed", s.getPassed());
                            item.put("submittedAt", s.getSubmittedAt());
                            item.put("gradedAt", s.getGradedAt());
                            item.put("feedback", s.getFeedback());
                            var names = new HashMap<UUID, String>();
                            rubric
                                .findByAssignmentIdOrderByPosition(a.getId())
                                .forEach(c -> names.put(c.getId(), c.getTitle()));
                            item.put(
                                "criterionGrades",
                                grades.findBySubmissionId(s.getId()).stream()
                                    .map(
                                        g ->
                                            Map.<String, Object>of(
                                                "criterionId",
                                                g.getCriterionId(),
                                                "title",
                                                names.getOrDefault(g.getCriterionId(), "Criterion"),
                                                "score",
                                                g.getScore(),
                                                "feedback",
                                                g.getFeedback()))
                                    .toList());
                            return item;
                          })
                      .toList());
              return row;
            })
        .toList();
  }
}
