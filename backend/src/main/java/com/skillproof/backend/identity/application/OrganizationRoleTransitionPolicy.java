package com.skillproof.backend.identity.application;

import java.util.UUID;

import com.skillproof.backend.identity.domain.UserRole;

/**
 * Implemented by the Organization module; Identity does not inspect
 * organization tables.
 */
public interface OrganizationRoleTransitionPolicy {

    void assertTransitionAllowed(UUID accountId, UserRole target);
}
