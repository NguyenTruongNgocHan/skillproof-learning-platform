package com.skillproof.backend.freeze;

import static org.assertj.core.api.Assertions.*;

import com.skillproof.backend.access.application.AccessEntitlementService;
import com.skillproof.backend.commerce.domain.ProductType;
import com.skillproof.backend.course.application.*;
import com.skillproof.backend.course.infrastructure.CourseContextRepository;
import com.skillproof.backend.identity.IdentityTestMailConfig;
import com.skillproof.backend.identity.domain.*;
import com.skillproof.backend.identity.infrastructure.UserAccountRepository;
import com.skillproof.backend.payment.application.*;
import com.skillproof.backend.payment.application.port.PaymentGateway;
import com.skillproof.backend.payment.infrastructure.persistence.PaymentOrderRepository;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.*;
import org.springframework.test.context.ActiveProfiles;

/** Provider signature cryptography is tested separately; here the gateway port is deterministic. */
@SpringBootTest
@ActiveProfiles("test")
@Import({IdentityTestMailConfig.class, PaymentFreezeIntegrationTest.GatewayConfig.class})
class PaymentFreezeIntegrationTest {
  @TestConfiguration
  static class GatewayConfig {
    @Bean
    @Primary
    PaymentGateway testGateway() {
      return new PaymentGateway() {
        public String checkout(UUID id, long amount, Instant created, Instant expiry, String ip) {
          return "https://payments.example.test/" + id;
        }

        public boolean authentic(Map<String, String> input) {
          return "test-signature".equals(input.get("signature"));
        }

        public String merchantCode() {
          return "TEST_MERCHANT";
        }
      };
    }
  }

  @Autowired UserAccountRepository users;
  @Autowired CourseAuthoringService courses;
  @Autowired CourseContentService content;
  @Autowired LessonService lessons;
  @Autowired CourseContextRepository versions;
  @Autowired PaymentOrderService orders;
  @Autowired PaymentOrderRepository payments;
  @Autowired VnPayNotificationService notifications;
  @Autowired AccessEntitlementService access;
  @Autowired CourseEnrollmentService enrollments;

  UUID account(UserRole role) {
    var user =
        role == UserRole.ADMIN
            ? UserAccount.newAdmin(
                UUID.randomUUID() + "@example.org", "hash", "Admin", Instant.now())
            : UserAccount.newAccount(UUID.randomUUID() + "@example.org", "hash", "User", role);
    if (role != UserRole.ADMIN) user.verifyEmail(Instant.now());
    return users.saveAndFlush(user).getId();
  }

  record Fixture(UUID learner, UUID course, UUID version) {}

  Fixture fixture() {
    UUID author = account(UserRole.LEARNER),
        admin = account(UserRole.ADMIN),
        learner = account(UserRole.LEARNER);
    UUID course =
        (UUID)
            courses
                .create(author, null, "paid-" + UUID.randomUUID(), "Paid course", "Summary")
                .get("id");
    UUID version =
        versions
            .findFirstByCourseIdAndStatusOrderByVersionNoDesc(course, "DRAFT")
            .orElseThrow()
            .getId();
    UUID module = (UUID) content.module(author, version, 1, "Module").get("id");
    lessons.create(author, module, 1, "Lesson", "Body");
    courses.configureOffer(author, version, "PUBLIC", 10000, 0);
    courses.submitReview(author, version);
    courses.review(admin, version, true, "Reviewed");
    courses.publish(author, version);
    return new Fixture(learner, course, version);
  }

  Map<String, String> notification(UUID order, Instant paidAt) {
    return new HashMap<>(
        Map.of(
            "signature",
            "test-signature",
            "vnp_TmnCode",
            "TEST_MERCHANT",
            "vnp_TxnRef",
            order.toString().replace("-", ""),
            "vnp_Amount",
            "1000000",
            "vnp_ResponseCode",
            "00",
            "vnp_TransactionStatus",
            "00",
            "vnp_TransactionNo",
            String.valueOf(Math.abs(new Random().nextLong(1, 999999999999L))),
            "vnp_PayDate",
            DateTimeFormatter.ofPattern("uuuuMMddHHmmss")
                .withZone(ZoneId.of("Asia/Ho_Chi_Minh"))
                .format(paidAt)));
  }

  @Test
  void concurrentSameKeyCreatesExactlyOneOrder() throws Exception {
    var f = fixture();
    try (var workers = Executors.newFixedThreadPool(2)) {
      var gate = new CountDownLatch(1);
      Callable<UUID> create =
          () -> {
            gate.await();
            return orders
                .create(f.learner(), ProductType.COURSE, f.version(), "same-key-001", "127.0.0.1")
                .orderId();
          };
      var a = workers.submit(create);
      var b = workers.submit(create);
      gate.countDown();
      assertThat(a.get(20, TimeUnit.SECONDS)).isEqualTo(b.get(20, TimeUnit.SECONDS));
    }
    assertThat(orders.mine(f.learner())).hasSize(1);
  }

  @Test
  void differentKeyCannotCreateAnotherPendingCheckout() {
    var f = fixture();
    orders.create(f.learner(), ProductType.COURSE, f.version(), "first-key-001", "127.0.0.1");
    assertThatThrownBy(
            () ->
                orders.create(
                    f.learner(), ProductType.COURSE, f.version(), "other-key-001", "127.0.0.1"))
        .isInstanceOf(com.skillproof.backend.common.exception.ConflictException.class);
  }

  @Test
  void authenticatedIpnGrantsAccessWithoutCreatingEnrollment() {
    var f = fixture();
    var order =
        orders.create(f.learner(), ProductType.COURSE, f.version(), "paid-key-001", "127.0.0.1");
    var ipn = notification(order.orderId(), Instant.now());
    assertThat(notifications.notify(ipn).RspCode()).isEqualTo("00");
    assertThat(notifications.notify(ipn).RspCode()).isEqualTo("02");
    assertThat(access.allowed(f.learner(), ProductType.COURSE, f.version())).isTrue();
    assertThat(enrollments.mine(f.learner())).isEmpty();
    assertThat(orders.detail(f.learner(), order.orderId()).getStatus()).isEqualTo("PAID");
    assertThatThrownBy(
            () ->
                orders.create(
                    f.learner(), ProductType.COURSE, f.version(), "owned-key-001", "127.0.0.1"))
        .isInstanceOf(com.skillproof.backend.common.exception.ConflictException.class);
  }

  @Test
  void signedBrowserReturnCannotGrantAccess() {
    var f = fixture();
    var order =
        orders.create(f.learner(), ProductType.COURSE, f.version(), "return-key-001", "127.0.0.1");
    notifications.browserReturn(notification(order.orderId(), Instant.now()));
    assertThat(access.allowed(f.learner(), ProductType.COURSE, f.version())).isFalse();
    assertThat(orders.detail(f.learner(), order.orderId()).getStatus()).isEqualTo("PENDING");
  }

  @Test
  void anotherLearnerCannotReadAnOrder() {
    var f = fixture();
    var order =
        orders.create(f.learner(), ProductType.COURSE, f.version(), "private-key-001", "127.0.0.1");
    assertThatThrownBy(() -> orders.detail(account(UserRole.LEARNER), order.orderId()))
        .isInstanceOf(com.skillproof.backend.common.exception.NotFoundException.class);
  }
}
