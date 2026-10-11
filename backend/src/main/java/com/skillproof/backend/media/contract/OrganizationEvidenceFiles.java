package com.skillproof.backend.media.contract;

import java.util.UUID;

public interface OrganizationEvidenceFiles {
    void requireFile(UUID organization, UUID media, String purpose);
}
