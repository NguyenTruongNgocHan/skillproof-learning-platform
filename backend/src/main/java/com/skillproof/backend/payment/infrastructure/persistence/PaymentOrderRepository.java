package com.skillproof.backend.payment.infrastructure.persistence;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentOrderRepository extends JpaRepository<PaymentOrderEntity, UUID> {

    Optional<PaymentOrderEntity> findByLearnerIdAndIdempotencyKey(
            UUID learnerId, String idempotencyKey);

    Optional<PaymentOrderEntity>
            findFirstByLearnerIdAndProductTypeAndProductIdAndStatusAndExpiresAtAfter(
                    UUID learnerId,
                    String productType,
                    UUID productId,
                    String status,
                    Instant time);

    List<PaymentOrderEntity> findByLearnerIdOrderByCreatedAtDesc(UUID learnerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from PaymentOrderEntity o where o.id = :id")
    Optional<PaymentOrderEntity> lock(UUID id);

    @org.springframework.data.jpa.repository.Query(
            "select count(o), coalesce(sum(o.amountVnd),0) from PaymentOrderEntity o where"
                + " o.organizationId=:organization and o.status='PAID' and o.settledAt>=:start and"
                + " o.settledAt<:end")
    java.util.List<Object[]> revenue(
            UUID organization, java.time.Instant start, java.time.Instant end);
}
