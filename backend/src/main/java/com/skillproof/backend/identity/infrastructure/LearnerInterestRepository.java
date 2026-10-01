package com.skillproof.backend.identity.infrastructure;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LearnerInterestRepository
        extends JpaRepository<LearnerInterestEntity, UUID> {

    List<LearnerInterestEntity> findAllByUserIdOrderByCreatedAtAsc(UUID userId);

    void deleteAllByUserId(UUID userId);
}