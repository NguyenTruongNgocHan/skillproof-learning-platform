package com.skillproof.backend.commerce.application;

import java.util.UUID;
import org.springframework.stereotype.Service;
import com.skillproof.backend.commerce.contract.ProductOfferQuery;
import com.skillproof.backend.commerce.domain.ProductType;
import com.skillproof.backend.course.contract.CourseOfferQuery;
import com.skillproof.backend.library.contract.LibraryOfferQuery;

@Service
public class ProductOfferService implements ProductOfferQuery {

    private final CourseOfferQuery courses;
    private final LibraryOfferQuery resources;

    public ProductOfferService(CourseOfferQuery courses, LibraryOfferQuery resources) {
        this.courses = courses;
        this.resources = resources;
    }

    @Override
    public Offer offer(ProductType type, UUID id) {
        return type == ProductType.RESOURCE ? resources.offer(id) : courses.offer(type, id);
    }
}
