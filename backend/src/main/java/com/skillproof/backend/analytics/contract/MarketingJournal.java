package com.skillproof.backend.analytics.contract;

import java.util.UUID;

public interface MarketingJournal {
    void conversion(UUID actor, UUID version, String kind, UUID reference);
}
