package com.skillproof.backend.payment.application;

import com.skillproof.backend.payment.infrastructure.persistence.PaymentOrderRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentExpiryService {
  private final PaymentOrderRepository orders;
  @jakarta.persistence.PersistenceContext private jakarta.persistence.EntityManager entityManager;

  public PaymentExpiryService(PaymentOrderRepository orders) {
    this.orders = orders;
  }

  @Transactional
  public void expire(UUID id) {
    var order = orders.lock(id).orElseThrow();
    entityManager.refresh(order, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
    order.expire(Instant.now());
  }
}
