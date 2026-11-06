package com.skillproof.backend.identity.contract;

import com.skillproof.backend.identity.domain.UserRole;
import java.util.UUID;

/** Implemented by the Organization module; Identity does not inspect organization tables. */
public interface OrganizationRoleTransitionPolicy {

  void assertTransitionAllowed(UUID accountId, UserRole target);
}
