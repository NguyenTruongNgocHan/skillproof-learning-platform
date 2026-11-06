package com.skillproof.backend.access.contract;

import com.skillproof.backend.commerce.domain.ProductType;
import java.util.UUID;

public interface PaymentEntitlementWriter {

  void grantPayment(UUID learnerId, ProductType type, UUID productId, UUID orderId);
}
