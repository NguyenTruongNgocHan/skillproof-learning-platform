package com.skillproof.backend.payment.infrastructure.persistence;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import jakarta.persistence.LockModeType;

public interface PaymentOrderRepository extends JpaRepository<PaymentOrderEntity, UUID> {

    Optional<PaymentOrderEntity> findByLearnerIdAndIdempotencyKey(UUID learnerId, String idempotencyKey);

    Optional<PaymentOrderEntity> findFirstByLearnerIdAndProductTypeAndProductIdAndStatusAndExpiresAtAfter(UUID learnerId, String productType, UUID productId, String status, Instant time);

    List<PaymentOrderEntity> findByLearnerIdOrderByCreatedAtDesc(UUID learnerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from PaymentOrderEntity o where o.id = :id")
    Optional<PaymentOrderEntity> lock(UUID id);

}
