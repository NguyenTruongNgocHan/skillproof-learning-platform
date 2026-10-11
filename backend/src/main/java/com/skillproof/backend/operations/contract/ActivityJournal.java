package com.skillproof.backend.operations.contract;

import java.util.UUID;

public interface ActivityJournal {
    void record(UUID actor, UUID organization, String action, UUID target, String details);

    void notify(UUID recipient, UUID organization, String category, String message, String link);
}
