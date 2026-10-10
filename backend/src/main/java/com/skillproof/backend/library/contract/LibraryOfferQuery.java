package com.skillproof.backend.library.contract;

import java.util.UUID;

import com.skillproof.backend.commerce.contract.ProductOfferQuery;

public interface LibraryOfferQuery {

    ProductOfferQuery.Offer offer(UUID resourceId);
}
