package com.skillproof.backend.payment.application.port;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public interface PaymentGateway {

    String checkout(UUID orderId, long amountVnd, Instant createdAt, Instant expiresAt, String ipAddress);

    boolean authentic(Map<String, String> parameters);

    String merchantCode();
}
