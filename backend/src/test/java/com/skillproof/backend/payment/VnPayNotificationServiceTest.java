package com.skillproof.backend.payment;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import com.skillproof.backend.access.contract.PaymentEntitlementWriter;
import com.skillproof.backend.commerce.domain.ProductType;
import com.skillproof.backend.payment.application.VnPayNotificationService;
import com.skillproof.backend.payment.application.port.PaymentGateway;
import com.skillproof.backend.payment.infrastructure.persistence.PaymentOrderEntity;
import com.skillproof.backend.payment.infrastructure.persistence.PaymentOrderRepository;

class VnPayNotificationServiceTest {
    private final PaymentGateway gateway = mock(PaymentGateway.class);
    private final PaymentOrderRepository orders = mock(PaymentOrderRepository.class);
    private final PaymentEntitlementWriter grants = mock(PaymentEntitlementWriter.class);
    private final VnPayNotificationService service = new VnPayNotificationService(gateway,orders,grants);
    private final UUID learner = UUID.randomUUID(), product = UUID.randomUUID(), id = UUID.randomUUID();
    private PaymentOrderEntity order;
    private Map<String,String> parameters;

    @BeforeEach void setup() {
        ReflectionTestUtils.setField(service,"entityManager",mock(jakarta.persistence.EntityManager.class));
        Instant now = Instant.now();
        order = new PaymentOrderEntity(id,learner,"COURSE",product,"checkout-001",10000,"PENDING",null,now.minusSeconds(60),now.plusSeconds(840),null);
        when(orders.lock(id)).thenReturn(Optional.of(order));
        when(gateway.authentic(anyMap())).thenReturn(true);
        when(gateway.merchantCode()).thenReturn("MERCHANT");
        parameters = new HashMap<>(Map.of("vnp_TmnCode","MERCHANT","vnp_TxnRef",id.toString().replace("-",""),
            "vnp_Amount","1000000","vnp_ResponseCode","00","vnp_TransactionStatus","00","vnp_TransactionNo","123456",
            "vnp_PayDate",DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withZone(ZoneId.of("Asia/Ho_Chi_Minh")).format(now)));
    }
    @Test void invalidSignatureDoesNotReadOrdersOrGrantAccess() {
        when(gateway.authentic(anyMap())).thenReturn(false);
        assertEquals("97",service.notify(parameters).RspCode());
        verifyNoInteractions(orders,grants);
    }
    @Test void invalidAmountDoesNotSettleOrGrantAccess() {
        parameters.put("vnp_Amount","100");
        assertEquals("04",service.notify(parameters).RspCode());
        assertEquals("PENDING",order.getStatus());
        verifyNoInteractions(grants);
    }
    @Test void verifiedSuccessGrantsOnceAndRepeatIsIdempotent() {
        assertEquals("00",service.notify(parameters).RspCode());
        assertEquals("PAID",order.getStatus());
        assertEquals("02",service.notify(parameters).RspCode());
        verify(grants,times(1)).grantPayment(learner,ProductType.COURSE,product,id);
        verify(orders,times(1)).saveAndFlush(order);
    }
    @Test void CancelledPaymentDoesNotGrantAccess() {
        parameters.put("vnp_ResponseCode","24");parameters.put("vnp_TransactionStatus","02");
        assertEquals("00",service.notify(parameters).RspCode());
        assertEquals("CANCELLED",order.getStatus());verifyNoInteractions(grants);
    }
    @Test void failedPaymentDoesNotGrantAccess() {
        parameters.put("vnp_ResponseCode","51");parameters.put("vnp_TransactionStatus","02");
        assertEquals("00",service.notify(parameters).RspCode());
        assertEquals("FAILED",order.getStatus());verifyNoInteractions(grants);
    }
    @Test void incompleteProviderStatusCanRetry() {
        parameters.put("vnp_TransactionStatus","01");
        assertEquals("99",service.notify(parameters).RspCode());
        assertEquals("PENDING",order.getStatus());verifyNoInteractions(grants);
    }
    @Test void paymentOutsideWindowCannotGrantAccess() {
        parameters.put("vnp_PayDate","20200101000000");
        assertEquals("99",service.notify(parameters).RspCode());verifyNoInteractions(grants);
    }
    @Test void signedBrowserReturnNeverMutatesOrderOrGrants() {
        assertEquals(true,service.browserReturn(parameters).get("signatureValid"));
        verifyNoInteractions(orders,grants);
    }
}
