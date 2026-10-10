package com.skillproof.backend.payment;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import com.skillproof.backend.payment.domain.*;
class PaymentDecisionTest {
    @Test void bothSuccessCodesAreRequired() {
        assertEquals(PaymentStatus.PAID,PaymentDecision.fromProvider("00","00"));
        assertEquals(PaymentStatus.FAILED,PaymentDecision.fromProvider("00","02"));
        assertEquals(PaymentStatus.FAILED,PaymentDecision.fromProvider("51","00"));
        assertEquals(PaymentStatus.CANCELLED,PaymentDecision.fromProvider("24","02"));
    }
}
