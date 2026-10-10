package com.skillproof.backend.commerce.contract;

import java.util.UUID;

import com.skillproof.backend.commerce.domain.ProductType;

public interface ProductOfferQuery {

    record Offer(ProductType type, UUID productId, UUID organizationId, String title,
            long priceVnd, boolean purchasable, boolean available) {

    }

    Offer offer(ProductType type, UUID productId);
}
