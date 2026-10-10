package com.skillproof.backend.payment.application;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillproof.backend.access.contract.PaymentEntitlementWriter;
import com.skillproof.backend.commerce.domain.ProductType;
import com.skillproof.backend.payment.application.port.PaymentGateway;
import com.skillproof.backend.payment.domain.PaymentDecision;
import com.skillproof.backend.payment.domain.PaymentStatus;
import com.skillproof.backend.payment.infrastructure.persistence.PaymentOrderRepository;

@Service
public class VnPayNotificationService {

    @jakarta.persistence.PersistenceContext
    private jakarta.persistence.EntityManager entityManager;
    private final PaymentGateway gateway;
    private final PaymentOrderRepository orders;
    private final PaymentEntitlementWriter entitlements;

    public VnPayNotificationService(PaymentGateway gateway, PaymentOrderRepository orders, PaymentEntitlementWriter entitlements) {
        this.gateway = gateway;
        this.orders = orders;
        this.entitlements = entitlements;
    }

    public record Acknowledgement(String RspCode, String Message) {

    }

    @Transactional
    public Acknowledgement notify(Map<String, String> parameters) {
        if (!gateway.authentic(parameters) || !gateway.merchantCode().equals(parameters.get("vnp_TmnCode"))) {
            return new Acknowledgement("97", "Invalid signature");
        }
        final UUID id;
        final long amount;
        try {
            String ref = parameters.get("vnp_TxnRef");
            if (ref == null || !ref.matches("[0-9a-fA-F]{32}")) {
                return new Acknowledgement("01", "Order not found");
            }
            id = UUID.fromString(ref.substring(0, 8) + "-" + ref.substring(8, 12) + "-" + ref.substring(12, 16) + "-" + ref.substring(16, 20) + "-" + ref.substring(20));
            amount = Long.parseLong(parameters.get("vnp_Amount"));
        } catch (RuntimeException e) {
            return new Acknowledgement("01", "Invalid order");
        }
        var found = orders.lock(id);
        if (found.isEmpty()) {
            return new Acknowledgement("01", "Order not found");
        }
        var order = found.get();
        entityManager.refresh(order, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
        if (amount != Math.multiplyExact(order.getAmountVnd(), 100)) {
            return new Acknowledgement("04", "Invalid amount");
        }
        if (!"PENDING".equals(order.getStatus())) {
            return new Acknowledgement("02", "Order already confirmed");
        }
        String response = parameters.get("vnp_ResponseCode"), transactionStatus = parameters.get("vnp_TransactionStatus");
        if (response == null || !response.matches("[0-9]{2}") || transactionStatus == null || !transactionStatus.matches("[0-9]{2}")
                || "01".equals(transactionStatus)) {
            return new Acknowledgement("99", "Incomplete provider status");
        }
        PaymentStatus status = PaymentDecision.fromProvider(parameters.get("vnp_ResponseCode"), parameters.get("vnp_TransactionStatus"));
        String transaction = parameters.get("vnp_TransactionNo");
        if (status == PaymentStatus.PAID) {
            if (transaction == null || !transaction.matches("[0-9]{1,15}")) {
                return new Acknowledgement("99", "Invalid transaction");
            }
            final Instant paidAt;
            try {
                paidAt = java.time.LocalDateTime.parse(parameters.get("vnp_PayDate"), DateTimeFormatter.ofPattern("uuuuMMddHHmmss").withResolverStyle(java.time.format.ResolverStyle.STRICT)).atZone(ZoneId.of("Asia/Ho_Chi_Minh")).toInstant();
            } catch (RuntimeException e) {
                return new Acknowledgement("99", "Invalid payment time");
            }
            if (paidAt.isBefore(order.getCreatedAt().minusSeconds(1)) || paidAt.isAfter(order.getExpiresAt())) {
                return new Acknowledgement("99", "Payment outside order window");
            }
            entitlements.grantPayment(order.getLearnerId(), ProductType.valueOf(order.getProductType()), order.getProductId(), order.getId());
        }
        order.settle(status, transaction, Instant.now());
        orders.saveAndFlush(order);
        return new Acknowledgement("00", "Confirm success");
    }

    public Map<String, Object> browserReturn(Map<String, String> parameters) {
        // Browser return never settles orders or grants access, even when signed.
        return Map.of("signatureValid", gateway.authentic(parameters), "message", "Read your authenticated order endpoint for the authoritative status");
    }
}
