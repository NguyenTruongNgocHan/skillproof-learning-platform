package com.skillproof.backend.quiz.application;

import com.skillproof.backend.feedback.contract.FeedbackSubjectProvider;
import com.skillproof.backend.quiz.infrastructure.persistence.*;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class QuestionFeedbackSubject implements FeedbackSubjectProvider {
    private final QuizQuestionVersionRepository versions;
    private final QuizQuestionRootRepository questions;
    private final QuizBankRepository banks;
    private final QuizAttemptQuestionRepository attempts;

    public QuestionFeedbackSubject(
            QuizQuestionVersionRepository versions,
            QuizQuestionRootRepository questions,
            QuizBankRepository banks,
            QuizAttemptQuestionRepository attempts) {
        this.versions = versions;
        this.questions = questions;
        this.banks = banks;
        this.attempts = attempts;
    }

    public String type() {
        return "QUESTION_VERSION";
    }

    public Subject subject(UUID target, UUID actor) {
        var v = versions.findById(target).orElseThrow();
        var root = questions.findById(v.getQuestionId()).orElseThrow();
        var b = banks.findById(root.getBankId()).orElseThrow();
        return new Subject(
                b.getOrganizationId(),
                b.getCreatedBy(),
                actor != null && attempts.experienced(actor, target) > 0,
                v.getStem());
    }
}
