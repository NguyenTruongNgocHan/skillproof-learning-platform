package com.skillproof.backend.access.contract;

import com.skillproof.backend.commerce.domain.ProductType;
import java.util.UUID;

public interface AccessEntitlementQuery {

  boolean allowed(UUID learnerId, ProductType type, UUID productId);

  void require(UUID learnerId, ProductType type, UUID productId);
}
