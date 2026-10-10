package com.skillproof.backend.payment.application;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillproof.backend.commerce.contract.ProductOfferQuery;
import com.skillproof.backend.commerce.domain.ProductType;
import com.skillproof.backend.common.exception.BadRequestException;
import com.skillproof.backend.common.exception.ConflictException;
import com.skillproof.backend.common.exception.NotFoundException;
import com.skillproof.backend.identity.contract.IdentityAccessQuery;
import com.skillproof.backend.payment.application.port.PaymentGateway;
import com.skillproof.backend.payment.infrastructure.persistence.PaymentOrderEntity;
import com.skillproof.backend.payment.infrastructure.persistence.PaymentOrderRepository;

@Service
public class PaymentOrderService {

    private final com.skillproof.backend.course.contract.CoursePlanQuery courses;
    private final com.skillproof.backend.access.contract.AccessEntitlementQuery entitlements;
    private final PaymentOrderRepository orders;
    private final IdentityAccessQuery identities;
    private final ProductOfferQuery offers;
    private final PaymentGateway gateway;

    public PaymentOrderService(PaymentOrderRepository orders, IdentityAccessQuery identities, ProductOfferQuery offers,
            PaymentGateway gateway, com.skillproof.backend.course.contract.CoursePlanQuery courses, com.skillproof.backend.access.contract.AccessEntitlementQuery entitlements) {
        this.entitlements = entitlements;
        this.courses = courses;
        this.orders = orders;
        this.identities = identities;
        this.offers = offers;
        this.gateway = gateway;
    }

    public record Checkout(UUID orderId, String status, long amountVnd, Instant expiresAt, String checkoutUrl) {

    }

    @Transactional
    public Checkout create(UUID learner, ProductType type, UUID product, String key, String ip) {
        if (!identities.isActiveLearner(learner)) {
            throw new AccessDeniedException("Active learner required");
        }
        if (key == null || !key.matches("[A-Za-z0-9_-]{8,100}")) {
            throw new BadRequestException("IDEMPOTENCY_KEY", "Use an 8–100 character idempotency key");
        }
        // Serialize create requests for this buyer, including absent-key races.
        identities.lockActiveAccount(learner);
        var existing = orders.findByLearnerIdAndIdempotencyKey(learner, key);
        if (existing.isPresent()) {
            var order = existing.get();
            if (!type.name().equals(order.getProductType()) || !product.equals(order.getProductId())) {
                throw new ConflictException("IDEMPOTENCY_REUSE", "Key already belongs to another product");
            }
            return checkout(order, ip);
        }
        var offer = offers.offer(type, product);
        if (type != ProductType.RESOURCE) {
            var item = courses.item(learner, product);
            courses.enrolledVersion(learner, item.courseId()).ifPresent(existingVersion -> {
                if (!product.equals(existingVersion)) {
                    throw new ConflictException("ENROLLMENT_PINNED", "An existing enrollment is pinned to another version");
                }
            });
        }
        if (!offer.available() || !offer.purchasable() || offer.priceVnd() < 5000) {
            throw new BadRequestException("PRODUCT_NOT_PAYABLE", "Paid publicly purchasable product required");
        }
        if (entitlements.allowed(learner, type, product)) {
            throw new ConflictException("PRODUCT_ALREADY_OWNED", "An effective entitlement already grants access");
        }
        if (type == ProductType.CERTIFICATION) {
            entitlements.require(learner, ProductType.COURSE, product);
        }
        Instant now = Instant.now();
        if (orders.findFirstByLearnerIdAndProductTypeAndProductIdAndStatusAndExpiresAtAfter(learner, type.name(), product, "PENDING", now).isPresent()) {
            throw new ConflictException("CHECKOUT_PENDING", "Use the existing pending order before starting another checkout");
        }
        var order = new PaymentOrderEntity(UUID.randomUUID(), learner, type.name(), product, key, offer.priceVnd(), "PENDING", null, now, now.plusSeconds(900), null);
        var result = checkout(order, ip); // Fail before saving when merchant configuration is missing.
        orders.save(order);
        return result;
    }

    private Checkout checkout(PaymentOrderEntity order, String ip) {
        String url = "PENDING".equals(order.getStatus()) && order.getExpiresAt().isAfter(Instant.now())
                ? gateway.checkout(order.getId(), order.getAmountVnd(), order.getCreatedAt(), order.getExpiresAt(), ip) : null;
        return new Checkout(order.getId(), order.getStatus(), order.getAmountVnd(), order.getExpiresAt(), url);
    }

    @Transactional(readOnly = true)
    public List<PaymentOrderEntity> mine(UUID learner) {
        return orders.findByLearnerIdOrderByCreatedAtDesc(learner);
    }

    @Transactional(readOnly = true)
    public PaymentOrderEntity detail(UUID learner, UUID id) {
        return orders.findById(id).filter(o -> learner.equals(o.getLearnerId()))
                .orElseThrow(() -> new NotFoundException("ORDER_NOT_FOUND", "Order not found"));
    }
}
