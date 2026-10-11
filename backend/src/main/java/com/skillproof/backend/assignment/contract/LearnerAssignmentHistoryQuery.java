package com.skillproof.backend.assignment.contract;

import java.util.*;

/** Internal read model. The calling workspace must authorize its course enrollment first. */
public interface LearnerAssignmentHistoryQuery {
  List<Map<String, Object>> history(UUID learner, UUID enrollment, UUID version);
}
