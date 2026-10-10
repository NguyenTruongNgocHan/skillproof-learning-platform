package com.skillproof.backend.access;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.skillproof.backend.access.application.AccessEntitlementService;
import com.skillproof.backend.access.infrastructure.persistence.AccessGrantRepository;
import com.skillproof.backend.commerce.contract.ProductOfferQuery;
import com.skillproof.backend.commerce.domain.ProductType;
import com.skillproof.backend.identity.contract.IdentityAccessQuery;

class AccessEntitlementServiceTest {

    private final AccessGrantRepository grants = mock(AccessGrantRepository.class);
    private final ProductOfferQuery offers = mock(ProductOfferQuery.class);
    private final IdentityAccessQuery users = mock(IdentityAccessQuery.class);
    private final AccessEntitlementService service = new AccessEntitlementService(grants, offers, users);
    private final UUID learner = UUID.randomUUID(), product = UUID.randomUUID();

    @BeforeEach
    void setup() {
        when(users.isActiveLearner(learner)).thenReturn(true);
        when(grants.findByLearnerIdAndProductTypeAndProductId(any(), anyString(), any())).thenReturn(List.of());
    }

    private void offer(ProductType type, long price, boolean publicSale, boolean available) {
        when(offers.offer(type, product)).thenReturn(new ProductOfferQuery.Offer(type, product, null, "Product", price, publicSale, available));
    }

    @Test
    void freePublicStudyIsAllowedForActiveAccount() {
        offer(ProductType.COURSE, 0, true, true);
        assertTrue(service.allowed(learner, ProductType.COURSE, product));
    }

    @Test
    void paidOrRestrictedStudyRequiresGrant() {
        offer(ProductType.COURSE, 10000, true, true);
        assertFalse(service.allowed(learner, ProductType.COURSE, product));
        offer(ProductType.COURSE, 0, false, true);
        assertFalse(service.allowed(learner, ProductType.COURSE, product));
    }

    @Test
    void freeStudyDoesNotImplyPaidCertificationRight() {
        offer(ProductType.COURSE, 0, true, true);
        offer(ProductType.CERTIFICATION, 10000, true, true);
        assertTrue(service.allowed(learner, ProductType.COURSE, product));
        assertFalse(service.allowed(learner, ProductType.CERTIFICATION, product));
    }

    @Test
    void unavailableOrganizationCourseAndInactiveAccountCannotAccess() {
        offer(ProductType.COURSE, 0, true, false);
        assertFalse(service.allowed(learner, ProductType.COURSE, product));
        when(users.isActiveLearner(learner)).thenReturn(false);
        offer(ProductType.COURSE, 0, true, true);
        assertFalse(service.allowed(learner, ProductType.COURSE, product));
    }
}
