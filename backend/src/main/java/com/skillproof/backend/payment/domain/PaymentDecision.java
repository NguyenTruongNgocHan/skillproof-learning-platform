package com.skillproof.backend.payment.domain;

public final class PaymentDecision {

    private PaymentDecision() {
    }

    public static PaymentStatus fromProvider(String response, String transaction) {
        if ("00".equals(response) && "00".equals(transaction)) {
            return PaymentStatus.PAID;
        }
        if ("24".equals(response)) {
            return PaymentStatus.CANCELLED;
        }
        return PaymentStatus.FAILED;
    }

    public static boolean finalStatus(PaymentStatus status) {
        return status != PaymentStatus.PENDING;
    }
}
