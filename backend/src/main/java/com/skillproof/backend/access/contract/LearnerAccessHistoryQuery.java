package com.skillproof.backend.access.contract;

import java.util.*;

public interface LearnerAccessHistoryQuery {
    List<Map<String, Object>> history(UUID learner, UUID version);
}
