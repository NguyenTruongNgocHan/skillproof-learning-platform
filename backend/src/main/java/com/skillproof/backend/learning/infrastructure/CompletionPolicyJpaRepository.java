package com.skillproof.backend.learning.infrastructure;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CompletionPolicyJpaRepository extends JpaRepository<CompletionPolicyEntity, UUID> {
}
