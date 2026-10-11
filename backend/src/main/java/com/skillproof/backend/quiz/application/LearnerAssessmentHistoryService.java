package com.skillproof.backend.quiz.application;

import com.skillproof.backend.quiz.contract.LearnerAssessmentHistoryQuery;
import com.skillproof.backend.quiz.infrastructure.persistence.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LearnerAssessmentHistoryService implements LearnerAssessmentHistoryQuery {
  private final QuizAssessmentRepository assessments;
  private final QuizAttemptRepository attempts;

  public LearnerAssessmentHistoryService(
      QuizAssessmentRepository assessments, QuizAttemptRepository attempts) {
    this.assessments = assessments;
    this.attempts = attempts;
  }

  @Transactional(readOnly = true)
  public List<Map<String, Object>> history(UUID learner, UUID enrollment, UUID version) {
    var activity = new HashMap<UUID, QuizAssessment>();
    assessments
        .findByVersionIdAndStatus(version, QuizAssessment.Status.PUBLISHED)
        .forEach(a -> activity.put(a.getId(), a));
    return attempts.findByEnrollmentIdAndLearnerIdOrderByStartedAtDesc(enrollment, learner).stream()
        .filter(t -> activity.containsKey(t.getAssessmentId()))
        .map(
            t -> {
              var a = activity.get(t.getAssessmentId());
              Map<String, Object> row = new LinkedHashMap<>();
              row.put("id", t.getId());
              row.put("assessmentId", a.getId());
              row.put("title", a.getTitle());
              row.put("kind", a.getKind());
              row.put("required", a.isRequiredForCompletion());
              row.put("status", t.getStatus());
              row.put("scorePercent", t.getScorePercent());
              row.put("passed", t.getPassed());
              row.put("startedAt", t.getStartedAt());
              row.put("deadlineAt", t.getDeadlineAt());
              row.put("submittedAt", t.getSubmittedAt());
              return row;
            })
        .toList();
  }
}
