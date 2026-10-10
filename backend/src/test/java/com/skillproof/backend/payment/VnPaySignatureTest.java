package com.skillproof.backend.payment;

import static org.junit.jupiter.api.Assertions.*;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import com.skillproof.backend.payment.infrastructure.vnpay.VnPaySignature;

class VnPaySignatureTest {
    @Test void sortsAndExcludesHashAndNonProviderFields() {
        assertEquals("vnp_Amount=1000000&vnp_OrderInfo=SkillProof+order",VnPaySignature.canonical(Map.of(
            "vnp_OrderInfo","SkillProof order","vnp_Amount","1000000","vnp_SecureHash","ignored","extra","ignored")));
    }
    @Test void detectsTamperingAndRejectsMalformedHash() {
        var data = new HashMap<>(Map.of("vnp_Amount","1000000","vnp_TxnRef","order"));
        data.put("vnp_SecureHash",VnPaySignature.sign("test-secret",data));
        assertTrue(VnPaySignature.verify("test-secret",data));
        data.put("vnp_Amount","100");assertFalse(VnPaySignature.verify("test-secret",data));
        data.put("vnp_SecureHash","00");assertFalse(VnPaySignature.verify("test-secret",data));
    }
}
