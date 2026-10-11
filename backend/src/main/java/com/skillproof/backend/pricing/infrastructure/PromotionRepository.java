package com.skillproof.backend.pricing.infrastructure;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

import java.time.*;
import java.util.*;

public interface PromotionRepository extends JpaRepository<CoursePromotion, UUID> {
    List<CoursePromotion> findByCourseIdOrderByStartsAtDesc(UUID course);
}
