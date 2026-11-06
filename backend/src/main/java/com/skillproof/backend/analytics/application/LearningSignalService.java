package com.skillproof.backend.analytics.application;

import com.skillproof.backend.analytics.contract.LearningSignalJournal;
import com.skillproof.backend.analytics.infrastructure.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;

@Service
public class LearningSignalService implements LearningSignalJournal {
  private final LearningSignalRepository signals;

  public LearningSignalService(LearningSignalRepository signals) {
    this.signals = signals;
  }

  @Override
  @Transactional(propagation = Propagation.MANDATORY)
  public void record(
      UUID learner, UUID version, String kind, UUID reference, Map<String, Object> details) {
    if (learner == null
        || version == null
        || reference == null
        || details == null
        || !Set.of(
                "ENROLLED",
                "RESOURCE_COMPLETED",
                "LESSON_COMPLETED",
                "ASSESSMENT_FINALIZED",
                "ASSIGNMENT_SUBMITTED",
                "ASSIGNMENT_GRADED",
                "COURSE_COMPLETED")
            .contains(kind)) throw new IllegalArgumentException("Invalid server learning signal");
    UUID id =
        UUID.nameUUIDFromBytes(
            ("skillproof.learning.v1:" + learner + ":" + version + ":" + kind + ":" + reference)
                .getBytes(StandardCharsets.UTF_8));
    if (!signals.existsById(id))
      signals.save(new LearningSignal(id, learner, version, kind, reference, details));
  }
}
