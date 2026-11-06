package com.skillproof.backend.course.contract;

import com.skillproof.backend.commerce.contract.ProductOfferQuery;
import com.skillproof.backend.commerce.domain.ProductType;
import java.util.UUID;

public interface CourseOfferQuery {

  ProductOfferQuery.Offer offer(ProductType type, UUID versionId);
}
