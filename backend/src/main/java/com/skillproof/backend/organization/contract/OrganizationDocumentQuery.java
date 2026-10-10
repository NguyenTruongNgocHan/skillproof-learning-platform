package com.skillproof.backend.organization.contract;

import java.util.List;
import java.util.UUID;

public interface OrganizationDocumentQuery {

    List<UUID> documentIds(UUID organizationId);
}
