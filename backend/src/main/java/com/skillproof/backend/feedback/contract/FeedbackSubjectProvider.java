package com.skillproof.backend.feedback.contract;

import java.util.*;

public interface FeedbackSubjectProvider {
    record Subject(UUID organizationId, UUID authorId, boolean experienced, String label) {}

    String type();

    Subject subject(UUID target, UUID actor);
}
