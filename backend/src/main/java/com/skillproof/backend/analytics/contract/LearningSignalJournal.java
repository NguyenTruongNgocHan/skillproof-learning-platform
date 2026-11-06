package com.skillproof.backend.analytics.contract;

import java.util.*;

/** Server-owned, versioned learning evidence for future recommendation experiments. */
public interface LearningSignalJournal {
  void record(
      UUID learner, UUID courseVersion, String kind, UUID reference, Map<String, Object> details);
}
