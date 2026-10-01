package com.skillproof.backend.recommendation.infrastructure;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface InteractionEventRepository
        extends JpaRepository<InteractionEventEntity, UUID> {
}