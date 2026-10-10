package com.skillproof.backend.access.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import jakarta.persistence.LockModeType;

public interface AccessGrantRepository extends JpaRepository<AccessGrantEntity, UUID> {

    List<AccessGrantEntity> findByLearnerIdAndProductTypeAndProductId(UUID learnerId, String productType, UUID productId);

    List<AccessGrantEntity> findByLearnerIdOrderByCreatedAtDesc(UUID learnerId);

    boolean existsByReferenceId(UUID referenceId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select g from AccessGrantEntity g where g.id = :id")
    Optional<AccessGrantEntity> lock(UUID id);

}
