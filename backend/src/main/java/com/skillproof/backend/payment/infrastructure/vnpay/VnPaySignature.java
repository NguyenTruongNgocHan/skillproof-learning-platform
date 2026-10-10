package com.skillproof.backend.payment.infrastructure.vnpay;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Map;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public final class VnPaySignature {

    private VnPaySignature() {
    }

    public static String canonical(Map<String, String> parameters) {
        return new TreeMap<>(parameters).entrySet().stream()
                .filter(e -> e.getKey().startsWith("vnp_") && !e.getKey().equals("vnp_SecureHash")
                && !e.getKey().equals("vnp_SecureHashType") && e.getValue() != null && !e.getValue().isEmpty())
                .map(e -> encode(e.getKey()) + "=" + encode(e.getValue()))
                .collect(java.util.stream.Collectors.joining("&"));
    }

    private static String encode(String text) {
        return URLEncoder.encode(text, StandardCharsets.US_ASCII);
    }

    public static String sign(String secret, Map<String, String> parameters) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            return HexFormat.of().formatHex(mac.doFinal(canonical(parameters).getBytes(StandardCharsets.UTF_8)));
        } catch (java.security.GeneralSecurityException e) {
            throw new IllegalStateException("Cannot sign VNPay request", e);
        }
    }

    public static boolean verify(String secret, Map<String, String> parameters) {
        String supplied = parameters.get("vnp_SecureHash");
        if (supplied == null || !supplied.matches("(?i)[0-9a-f]{128}")) {
            return false;
        }
        return MessageDigest.isEqual(HexFormat.of().parseHex(supplied), HexFormat.of().parseHex(sign(secret, parameters)));
    }
}
