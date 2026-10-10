package com.skillproof.backend.access.application;

import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;
import com.skillproof.backend.access.contract.AccessEntitlementQuery;
import com.skillproof.backend.access.infrastructure.persistence.AccessGrantRepository;
import com.skillproof.backend.commerce.contract.ProductOfferQuery;
import com.skillproof.backend.commerce.domain.ProductType;
import com.skillproof.backend.identity.contract.IdentityAccessQuery;

@Service
public class AccessEntitlementService implements AccessEntitlementQuery {

    private final AccessGrantRepository grants;
    private final ProductOfferQuery offers;
    private final IdentityAccessQuery identities;

    public AccessEntitlementService(AccessGrantRepository grants, ProductOfferQuery offers, IdentityAccessQuery identities) {
        this.grants = grants;
        this.offers = offers;
        this.identities = identities;
    }

    @Override
    public boolean allowed(UUID learnerId, ProductType type, UUID productId) {
        if (!identities.isActiveLearner(learnerId)) {
            return false;
        }
        var offer = offers.offer(type, productId);
        if (!offer.available()) {
            return false;
        }
        if (offer.purchasable() && offer.priceVnd() == 0) {
            return true;
        }
        return grants.findByLearnerIdAndProductTypeAndProductId(learnerId, type.name(), productId)
                .stream().anyMatch(g -> g.effective(Instant.now()));
    }

    @Override
    public void require(UUID learnerId, ProductType type, UUID productId) {
        if (!allowed(learnerId, type, productId)) {
            throw new AccessDeniedException("Access entitlement required");
        }
    }
}
