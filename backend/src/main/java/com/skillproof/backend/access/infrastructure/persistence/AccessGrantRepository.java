package com.skillproof.backend.access.infrastructure.persistence;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccessGrantRepository extends JpaRepository<AccessGrantEntity, UUID> {

    List<AccessGrantEntity> findByLearnerIdAndProductTypeAndProductId(
            UUID learnerId, String productType, UUID productId);

    List<AccessGrantEntity> findByLearnerIdOrderByCreatedAtDesc(UUID learnerId);

    org.springframework.data.domain.Page<AccessGrantEntity>
            findByOrganizationIdOrderByCreatedAtDesc(
                    UUID org, org.springframework.data.domain.Pageable page);

    boolean existsByReferenceId(UUID referenceId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select g from AccessGrantEntity g where g.id = :id")
    Optional<AccessGrantEntity> lock(UUID id);

    @Query(
            "select g from AccessGrantEntity g where g.organizationId=:organization and (:query=''"
                + " or lower(g.cohort) like concat('%',lower(:query),'%') or cast(g.learnerId as"
                + " string) like concat('%',:query,'%') or cast(g.productId as string) like"
                + " concat('%',:query,'%')) and (:status='' or (:status='REVOKED' and g.revokedAt"
                + " is not null) or (:status='ACTIVE' and g.revokedAt is null and (g.expiresAt is"
                + " null or g.expiresAt>:now)) or (:status='EXPIRED' and g.revokedAt is null and"
                + " g.expiresAt<=:now)) order by g.createdAt desc,g.id")
    org.springframework.data.domain.Page<AccessGrantEntity> search(
            UUID organization,
            String query,
            String status,
            java.time.Instant now,
            org.springframework.data.domain.Pageable pageable);
}
