package com.skillproof.backend.library.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import jakarta.persistence.LockModeType;

public interface LibraryResourceRepository extends JpaRepository<LibraryResourceEntity, UUID> {

    Page<LibraryResourceEntity> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);

    List<LibraryResourceEntity> findByOrganizationIdOrderByCreatedAtDesc(UUID organizationId);

    List<LibraryResourceEntity> findByAuthorIdOrderByCreatedAtDesc(UUID authorId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from LibraryResourceEntity r where r.id = :id")
    Optional<LibraryResourceEntity> lock(UUID id);

}
