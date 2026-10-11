package com.skillproof.backend.access.application;

import com.skillproof.backend.access.contract.LearnerAccessHistoryQuery;
import com.skillproof.backend.access.infrastructure.persistence.AccessGrantRepository;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class LearnerAccessHistoryService implements LearnerAccessHistoryQuery {
    private final AccessGrantRepository grants;

    public LearnerAccessHistoryService(AccessGrantRepository grants) {
        this.grants = grants;
    }

    public List<Map<String, Object>> history(UUID learner, UUID version) {
        return java.util.stream.Stream.of("COURSE", "CERTIFICATION")
                .flatMap(
                        type ->
                                grants
                                        .findByLearnerIdAndProductTypeAndProductId(
                                                learner, type, version)
                                        .stream())
                .map(
                        g -> {
                            Map<String, Object> m = new LinkedHashMap<>();
                            m.put("id", g.getId());
                            m.put("type", g.getProductType());
                            m.put("source", g.getSource());
                            m.put("cohort", g.getCohort());
                            m.put("createdAt", g.getCreatedAt());
                            m.put("expiresAt", g.getExpiresAt());
                            m.put("revokedAt", g.getRevokedAt());
                            return m;
                        })
                .toList();
    }
}
