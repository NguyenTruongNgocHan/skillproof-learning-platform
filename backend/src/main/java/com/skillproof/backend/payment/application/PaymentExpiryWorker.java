package com.skillproof.backend.payment.application;

import com.skillproof.backend.payment.infrastructure.persistence.PaymentOrderRepository;
import java.time.Instant;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class PaymentExpiryWorker {
  private final PaymentOrderRepository orders;
  private final PaymentExpiryService expiry;
  private static final org.slf4j.Logger LOG =
      org.slf4j.LoggerFactory.getLogger(PaymentExpiryWorker.class);

  public PaymentExpiryWorker(PaymentOrderRepository orders, PaymentExpiryService expiry) {
    this.orders = orders;
    this.expiry = expiry;
  }

  @Scheduled(fixedDelayString = "${skillproof.payment.expiry-delay:60000}")
  public void reconcile() {
    for (var order :
        orders.findByStatusAndExpiresAtBefore("PENDING", Instant.now(), PageRequest.of(0, 100))) {
      try {
        expiry.expire(order.getId());
      } catch (RuntimeException failure) {
        LOG.warn("Payment expiry reconciliation failed for {}", order.getId(), failure);
      }
    }
  }
}
