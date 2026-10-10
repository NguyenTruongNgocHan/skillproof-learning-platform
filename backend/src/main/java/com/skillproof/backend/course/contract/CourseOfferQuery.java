package com.skillproof.backend.course.contract;

import java.util.UUID;

import com.skillproof.backend.commerce.contract.ProductOfferQuery;
import com.skillproof.backend.commerce.domain.ProductType;

public interface CourseOfferQuery {

    ProductOfferQuery.Offer offer(ProductType type, UUID versionId);
}
