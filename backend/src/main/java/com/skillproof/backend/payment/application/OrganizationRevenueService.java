package com.skillproof.backend.payment.application;

import com.skillproof.backend.payment.contract.OrganizationRevenueQuery;
import com.skillproof.backend.payment.infrastructure.persistence.PaymentOrderRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class OrganizationRevenueService implements OrganizationRevenueQuery {
    private final PaymentOrderRepository orders;

    public OrganizationRevenueService(PaymentOrderRepository orders) {
        this.orders = orders;
    }

    @Transactional(readOnly = true)
    public Revenue revenue(UUID org, Instant start, Instant end) {
        var row = orders.revenue(org, start, end).get(0);
        return new Revenue(((Number) row[0]).longValue(), ((Number) row[1]).longValue());
    }
}
