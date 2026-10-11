package com.skillproof.backend.feedback.infrastructure;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

import java.time.*;
import java.util.*;

public interface ExperienceRepository extends JpaRepository<ExperienceReceipt, UUID> {
    boolean existsByActorIdAndTypeAndTargetId(UUID actor, String type, UUID target);
}
