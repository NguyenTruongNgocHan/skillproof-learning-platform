package com.skillproof.backend.media.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import jakarta.persistence.LockModeType;

public interface StorageObjectRepository extends JpaRepository<StorageObjectEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from StorageObjectEntity o where o.id = :id")
    Optional<StorageObjectEntity> lock(UUID id);

}
