package com.skillproof.backend.assignment.application;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.skillproof.backend.assignment.contract.AssignmentEvidenceQuery;
import com.skillproof.backend.assignment.infrastructure.persistence.AssignmentRepository;
import com.skillproof.backend.assignment.infrastructure.persistence.AssignmentSubmissionRepository;

@Service
public class AssignmentEvidenceService implements AssignmentEvidenceQuery {

    private final AssignmentRepository assignments;
    private final AssignmentSubmissionRepository submissions;

    public AssignmentEvidenceService(AssignmentRepository assignments, AssignmentSubmissionRepository submissions) {
        this.assignments = assignments;
        this.submissions = submissions;
    }

    @Override
    public Evidence evidence(UUID version, UUID enrollment) {
        return new Evidence(Math.toIntExact(assignments.countByVersionIdAndRequiredTrue(version)), Math.toIntExact(submissions.countPassed(version, enrollment)));
    }
}
