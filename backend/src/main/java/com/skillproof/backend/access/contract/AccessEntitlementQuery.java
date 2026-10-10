package com.skillproof.backend.access.contract;

import java.util.UUID;
import com.skillproof.backend.commerce.domain.ProductType;

public interface AccessEntitlementQuery {

    boolean allowed(UUID learnerId, ProductType type, UUID productId);

    void require(UUID learnerId, ProductType type, UUID productId);
}
