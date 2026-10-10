package com.skillproof.backend.payment.api;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.skillproof.backend.commerce.domain.ProductType;
import com.skillproof.backend.payment.application.PaymentOrderService;
import com.skillproof.backend.payment.application.VnPayNotificationService;
import com.skillproof.backend.payment.infrastructure.persistence.PaymentOrderEntity;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentOrderService orders;
    private final VnPayNotificationService notifications;

    public PaymentController(PaymentOrderService orders, VnPayNotificationService notifications) {
        this.orders = orders;
        this.notifications = notifications;
    }

    public record OrderInput(@NotNull ProductType type, @NotNull UUID productId) {

    }

    @PostMapping("/orders")
    public PaymentOrderService.Checkout checkout(Authentication auth, @RequestHeader("Idempotency-Key") String key,
            @Valid @RequestBody OrderInput input, HttpServletRequest request) {
        return orders.create((UUID) auth.getPrincipal(), input.type(), input.productId(), key, request.getRemoteAddr());
    }

    @GetMapping("/me/orders")
    public List<PaymentOrderEntity> mine(Authentication auth) {
        return orders.mine((UUID) auth.getPrincipal());
    }

    @GetMapping("/orders/{id}")
    public PaymentOrderEntity detail(Authentication auth, @PathVariable UUID id) {
        return orders.detail((UUID) auth.getPrincipal(), id);
    }

    @GetMapping("/vnpay/ipn")
    public VnPayNotificationService.Acknowledgement ipn(@RequestParam org.springframework.util.MultiValueMap<String, String> parameters) {
        if (parameters.values().stream().anyMatch(values -> values.size() != 1)) {
            return new VnPayNotificationService.Acknowledgement("97", "Duplicate parameters");
        }
        return notifications.notify(parameters.toSingleValueMap());
    }

    @GetMapping("/vnpay/return")
    public Map<String, Object> browserReturn(@RequestParam org.springframework.util.MultiValueMap<String, String> parameters) {
        if (parameters.values().stream().anyMatch(values -> values.size() != 1)) {
            return Map.of("signatureValid", false);
        }
        return notifications.browserReturn(parameters.toSingleValueMap());
    }
}
