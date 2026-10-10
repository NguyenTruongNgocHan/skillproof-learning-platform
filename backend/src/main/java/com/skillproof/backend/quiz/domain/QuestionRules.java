package com.skillproof.backend.quiz.domain;

import java.util.List;

/**
 * Question quality rules independent of API validation and persistence.
 */
public final class QuestionRules {

    private QuestionRules() {
    }

    public record Choice(String body, boolean correct) {

    }

    public static void validate(List<Choice> choices) {
        if (choices == null || choices.size() < 2 || choices.size() > 8 || choices.stream().anyMatch(java.util.Objects::isNull)
                || choices.stream().filter(Choice::correct).count() != 1) {
            throw new IllegalArgumentException("Provide 2–8 choices with exactly one correct answer");
        }
        if (choices.stream().anyMatch(c -> c.body() == null || c.body().isBlank() || c.body().length() > 1000)) {
            throw new IllegalArgumentException("Choice text must be 1–1000 characters");
        }
    }
}
