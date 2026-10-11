package com.skillproof.backend.certification.infrastructure;

import com.skillproof.backend.certification.contract.LearnerCourseCredentialsQuery;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class LearnerCourseCredentialsAdapter implements LearnerCourseCredentialsQuery {
    private final CertificateJpaRepository certificates;

    public LearnerCourseCredentialsAdapter(CertificateJpaRepository certificates) {
        this.certificates = certificates;
    }

    public List<Map<String, Object>> credentials(UUID learner, UUID version) {
        return certificates.findByLearnerUserIdAndCourseVersionId(learner, version).stream()
                .map(
                        e -> {
                            var c = e.domain();
                            Map<String, Object> m = new LinkedHashMap<>();
                            m.put("id", c.id());
                            m.put("serial", c.serialNumber());
                            m.put("status", c.status().name());
                            m.put("program", c.programName());
                            m.put("issuedAt", c.issuedAt());
                            m.put("revokedAt", c.revokedAt());
                            m.put("reason", c.revocationReason());
                            return m;
                        })
                .toList();
    }
}
