package com.skillproof.backend.pricing.infrastructure;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

import java.time.*;
import java.util.*;

public interface PriceChangeRepository extends JpaRepository<PriceChange, UUID> {
    List<PriceChange> findByCourseIdOrderByCreatedAtDesc(UUID course);
}
