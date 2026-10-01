package com.skillproof.backend.identity.infrastructure;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LearnerPreferencesRepository extends JpaRepository<LearnerPreferencesEntity, UUID> {
}
