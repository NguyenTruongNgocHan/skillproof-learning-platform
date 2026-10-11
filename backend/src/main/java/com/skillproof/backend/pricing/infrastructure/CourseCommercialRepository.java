package com.skillproof.backend.pricing.infrastructure;

import jakarta.persistence.LockModeType;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

import java.time.*;
import java.util.*;

public interface CourseCommercialRepository extends JpaRepository<CourseCommercial, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from CourseCommercial c where c.id=:id")
    Optional<CourseCommercial> lock(UUID id);
}
