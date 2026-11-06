package com.skillproof.backend.pricing.infrastructure;

import java.time.*;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

public interface PriceChangeRepository extends JpaRepository<PriceChange, UUID> {
  List<PriceChange> findByCourseIdOrderByCreatedAtDesc(UUID course);
}
