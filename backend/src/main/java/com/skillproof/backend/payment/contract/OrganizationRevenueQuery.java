package com.skillproof.backend.payment.contract;

import java.time.Instant;
import java.util.UUID;

public interface OrganizationRevenueQuery {
    record Revenue(long paidOrders, long grossPaidVnd) {}

    Revenue revenue(UUID organization, Instant start, Instant end);
}
