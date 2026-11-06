package com.skillproof.backend.library.contract;

import com.skillproof.backend.commerce.contract.ProductOfferQuery;
import java.util.UUID;

public interface LibraryOfferQuery {

  ProductOfferQuery.Offer offer(UUID resourceId);
}
