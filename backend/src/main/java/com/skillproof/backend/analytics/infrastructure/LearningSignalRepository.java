package com.skillproof.backend.analytics.infrastructure;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LearningSignalRepository extends JpaRepository<LearningSignal, UUID> {}
