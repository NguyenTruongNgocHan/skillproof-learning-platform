package com.skillproof.backend.organization.domain;

import java.util.List;
import java.util.UUID;

public record OrganizationMemberView(UUID userId, boolean active, String email,
        List<String> grants) {

}
