package com.skillproof.backend.feedback.application;

import com.skillproof.backend.feedback.contract.ExperienceJournal;
import com.skillproof.backend.feedback.infrastructure.*;
import com.skillproof.backend.identity.contract.IdentityAccessQuery;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;

@Service
public class ExperienceJournalService implements ExperienceJournal {
    private final ExperienceRepository receipts;
    private final IdentityAccessQuery identity;

    public ExperienceJournalService(ExperienceRepository receipts, IdentityAccessQuery identity) {
        this.receipts = receipts;
        this.identity = identity;
    }

    public boolean hasAccessed(UUID actor, String type, UUID target) {
        return receipts.existsByActorIdAndTypeAndTargetId(actor, type, target);
    }

    @Transactional
    public void accessed(UUID actor, String type, UUID target) {
        identity.lockActiveAccount(actor);
        if (!hasAccessed(actor, type, target))
            receipts.save(
                    new ExperienceReceipt(UUID.randomUUID(), actor, type, target, Instant.now()));
    }
}
