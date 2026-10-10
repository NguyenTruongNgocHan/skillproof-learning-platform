package com.skillproof.backend.learningpath.domain;

public final class PathRules {

    private PathRules() {
    }

    public static boolean validPrerequisite(int position, Integer prerequisitePosition) {
        return position > 0 && (prerequisitePosition == null || prerequisitePosition > 0 && prerequisitePosition < position);
    }
}
