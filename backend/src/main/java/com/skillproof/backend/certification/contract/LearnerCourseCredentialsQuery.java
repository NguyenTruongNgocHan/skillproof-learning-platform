package com.skillproof.backend.certification.contract;

import java.util.*;

public interface LearnerCourseCredentialsQuery {
    List<Map<String, Object>> credentials(UUID learner, UUID version);
}
