package com.skillproof.backend.pricing.infrastructure;

import java.time.*;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

public interface PromotionRepository extends JpaRepository<CoursePromotion, UUID> {
  List<CoursePromotion> findByCourseIdOrderByStartsAtDesc(UUID course);
}
