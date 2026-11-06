package com.skillproof.backend.media.contract;

import java.util.UUID;

public interface OrganizationEvidenceFiles {

  void requireFile(UUID organization, UUID media, String purpose);

  void requireImage(UUID organization, UUID media);

  String fingerprint(UUID organization, UUID media);
}
