package com.skillproof.backend.assignment.domain;

public final class SubmissionRules {

    private SubmissionRules() {
    }

    public static boolean passes(int score, int threshold) {
        if (score < 0 || score > 100 || threshold < 1 || threshold > 100) {
            throw new IllegalArgumentException("Scores must be percentages");
        }
        return score >= threshold;
    }
}
