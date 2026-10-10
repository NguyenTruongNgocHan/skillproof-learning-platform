package com.skillproof.backend.payment.infrastructure.vnpay;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.skillproof.backend.payment.application.port.PaymentGateway;

@Component
public class VnPayGateway implements PaymentGateway {

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withZone(ZoneId.of("Asia/Ho_Chi_Minh"));
    private final String merchant;
    private final String secret;
    private final String endpoint;
    private final String returnUrl;

    public VnPayGateway(@Value("${skillproof.payment.vnpay.merchant:}") String merchant,
            @Value("${skillproof.payment.vnpay.secret:}") String secret,
            @Value("${skillproof.payment.vnpay.url:https://sandbox.vnpayment.vn/paymentv2/vpcpay.html}") String endpoint,
            @Value("${skillproof.payment.vnpay.return-url:}") String returnUrl) {
        this.merchant = merchant;
        this.secret = secret;
        this.endpoint = endpoint;
        this.returnUrl = returnUrl;
    }

    @Override
    public String checkout(UUID id, long amount, Instant created, Instant expires, String ip) {
        if (merchant.isBlank() || secret.isBlank() || !returnUrl.startsWith("https://") || !endpoint.startsWith("https://")) {
            throw new IllegalStateException("VNPay HTTPS merchant configuration required");
        }
        var data = new TreeMap<String, String>();
        data.put("vnp_Version", "2.1.0");
        data.put("vnp_Command", "pay");
        data.put("vnp_TmnCode", merchant);
        data.put("vnp_Amount", Long.toString(Math.multiplyExact(amount, 100)));
        data.put("vnp_CurrCode", "VND");
        data.put("vnp_TxnRef", id.toString().replace("-", ""));
        data.put("vnp_OrderInfo", "SkillProof order " + id.toString().replace("-", ""));
        data.put("vnp_OrderType", "other");
        data.put("vnp_Locale", "vn");
        data.put("vnp_ReturnUrl", returnUrl);
        data.put("vnp_IpAddr", ip);
        data.put("vnp_CreateDate", TIME.format(created));
        data.put("vnp_ExpireDate", TIME.format(expires));
        return endpoint + "?" + VnPaySignature.canonical(data) + "&vnp_SecureHash=" + VnPaySignature.sign(secret, data);
    }

    @Override
    public boolean authentic(Map<String, String> parameters) {
        return !secret.isBlank() && VnPaySignature.verify(secret, parameters);
    }

    @Override
    public String merchantCode() {
        return merchant;
    }
}
