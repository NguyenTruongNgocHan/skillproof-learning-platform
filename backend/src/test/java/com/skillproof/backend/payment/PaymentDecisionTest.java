package com.skillproof.backend.payment;

import static org.junit.jupiter.api.Assertions.*;

import com.skillproof.backend.payment.domain.*;
import org.junit.jupiter.api.Test;

class PaymentDecisionTest {
  @Test
  void bothSuccessCodesAreRequired() {
    assertEquals(PaymentStatus.PAID, PaymentDecision.fromProvider("00", "00"));
    assertEquals(PaymentStatus.FAILED, PaymentDecision.fromProvider("00", "02"));
    assertEquals(PaymentStatus.FAILED, PaymentDecision.fromProvider("51", "00"));
    assertEquals(PaymentStatus.CANCELLED, PaymentDecision.fromProvider("24", "02"));
  }
}
