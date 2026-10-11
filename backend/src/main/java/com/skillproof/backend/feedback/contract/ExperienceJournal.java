package com.skillproof.backend.feedback.contract;

import java.util.UUID;

public interface ExperienceJournal {
    boolean hasAccessed(UUID actor, String type, UUID target);

    void accessed(UUID actor, String type, UUID target);
}
