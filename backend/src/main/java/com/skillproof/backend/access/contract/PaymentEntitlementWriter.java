package com.skillproof.backend.access.contract;

import java.util.UUID;
import com.skillproof.backend.commerce.domain.ProductType;

public interface PaymentEntitlementWriter {

    void grantPayment(UUID learnerId, ProductType type, UUID productId, UUID orderId);
}
