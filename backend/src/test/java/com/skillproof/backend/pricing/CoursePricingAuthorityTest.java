package com.skillproof.backend.pricing;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.skillproof.backend.course.contract.CourseWorkspaceQuery;
import com.skillproof.backend.operations.contract.ActivityJournal;
import com.skillproof.backend.organization.contract.OrganizationAuthorityQuery;
import com.skillproof.backend.pricing.application.CoursePricingService;
import com.skillproof.backend.pricing.infrastructure.*;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

class CoursePricingAuthorityTest {
  private final CourseWorkspaceQuery courses = mock(CourseWorkspaceQuery.class);
  private final OrganizationAuthorityQuery grants = mock(OrganizationAuthorityQuery.class);
  private final CoursePricingService service =
      new CoursePricingService(
          mock(CourseCommercialRepository.class),
          mock(PriceChangeRepository.class),
          mock(PromotionRepository.class),
          courses,
          grants,
          mock(ActivityJournal.class));
  private final UUID actor = UUID.randomUUID(),
      org = UUID.randomUUID(),
      version = UUID.randomUUID();

  @BeforeEach
  void fixture() {
    when(courses.context(version))
        .thenReturn(
            new CourseWorkspaceQuery.Context(
                UUID.randomUUID(),
                version,
                org,
                UUID.randomUUID(),
                "PUBLISHED",
                "Course",
                10000,
                0,
                "PUBLIC"));
  }

  @Test
  void pricingOnlyMemberCanReadWithoutAuthoringAuthority() {
    when(grants.hasAuthority(org, actor, "MANAGE_PRICING")).thenReturn(true);
    assertThat(service.detail(actor, version))
        .containsKeys("baseOffer", "offer", "history", "promotions");
    verify(courses, never()).requireAuthor(any(), any());
  }

  @Test
  void contentMemberCanInspectOfferWithoutChangingIt() {
    when(grants.hasAuthority(org, actor, "MANAGE_CONTENT")).thenReturn(true);
    assertThat(service.detail(actor, version)).containsKey("offer");
    assertThatThrownBy(() -> service.configure(actor, version, "PUBLIC", 10000, 0, "Reason"))
        .isInstanceOf(AccessDeniedException.class);
  }

  @Test
  void membershipWithoutRelevantAuthorityDoesNotExposeCommercialHistory() {
    assertThatThrownBy(() -> service.detail(actor, version))
        .isInstanceOf(AccessDeniedException.class);
  }

  @Test
  void pricingAuthorityDoesNotGrantCourseLifecycleAuthority() {
    when(grants.hasAuthority(org, actor, "MANAGE_PRICING")).thenReturn(true);
    assertThatThrownBy(() -> service.lifecycle(actor, version, "CLOSED"))
        .isInstanceOf(AccessDeniedException.class);
  }
}
