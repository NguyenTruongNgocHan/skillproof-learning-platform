package com.skillproof.backend.quiz.application;

import com.skillproof.backend.course.contract.CourseWorkspaceQuery;
import com.skillproof.backend.feedback.contract.FeedbackSubjectProvider;
import com.skillproof.backend.quiz.infrastructure.persistence.*;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AssessmentFeedbackSubject implements FeedbackSubjectProvider {
    private final QuizAssessmentRepository assessments;
    private final QuizAttemptRepository attempts;
    private final CourseWorkspaceQuery courses;

    public AssessmentFeedbackSubject(
            QuizAssessmentRepository assessments,
            QuizAttemptRepository attempts,
            CourseWorkspaceQuery courses) {
        this.assessments = assessments;
        this.attempts = attempts;
        this.courses = courses;
    }

    public String type() {
        return "ASSESSMENT";
    }

    public Subject subject(UUID target, UUID actor) {
        var a = assessments.findById(target).orElseThrow();
        var c = courses.context(a.getVersionId());
        return new Subject(
                c.organizationId(),
                c.author(),
                actor != null
                        && attempts.existsByAssessmentIdAndLearnerIdAndStatusIn(
                                target,
                                actor,
                                List.of(
                                        QuizAttempt.AttemptStatus.SCORED,
                                        QuizAttempt.AttemptStatus.TIMED_OUT)),
                a.getTitle());
    }
}
