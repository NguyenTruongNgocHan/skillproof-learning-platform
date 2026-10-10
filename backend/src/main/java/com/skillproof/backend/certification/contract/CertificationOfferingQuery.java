package com.skillproof.backend.certification.contract;

import java.util.Optional;
import java.util.UUID;

public interface CertificationOfferingQuery {

    record Offering(UUID programId, String name) {

    }

    boolean activeForVersion(UUID versionId);

    Optional<Offering> offeringForVersion(UUID versionId);
}
