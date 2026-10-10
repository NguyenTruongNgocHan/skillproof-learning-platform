package com.skillproof.backend.course.domain;

/**
 * Pure completion policy. Infrastructure supplies authoritative counts under an
 * enrollment lock.
 */
public final class CompletionRules {

    private CompletionRules() {
    }

    public static boolean completed(boolean requireResources, boolean requireAssessment,
            int resources, int resourcesDone, int assessments, int assessmentsPassed,
            int assignments, int assignmentsPassed, int lessons, int lessonsDone) {
        if (resources < 0 || resourcesDone < 0 || resourcesDone > resources || assessments < 0 || assessmentsPassed < 0
                || assessmentsPassed > assessments || assignments < 0 || assignmentsPassed < 0 || assignmentsPassed > assignments
                || lessons <= 0 || lessonsDone < 0 || lessonsDone > lessons) {
            return false;
        }
        return (!requireResources || resourcesDone == resources)
                && assessmentsPassed == assessments && (!requireAssessment || assessments > 0)
                && assignmentsPassed == assignments && lessonsDone == lessons;
    }
}
