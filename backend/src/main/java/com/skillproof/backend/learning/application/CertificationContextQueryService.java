package com.skillproof.backend.learning.application;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.skillproof.backend.common.exception.NotFoundException;
import com.skillproof.backend.learning.contract.CertificationContextQuery;

@Service
public class CertificationContextQueryService implements CertificationContextQuery {

    private final LearningContextReader contextReader;

    public CertificationContextQueryService(LearningContextReader contextReader) {
        this.contextReader = contextReader;
    }

    @Override
    public CertificationContext requireCertificationContext(UUID learningPathVersionId) {
        return contextReader.certification(learningPathVersionId)
                .orElseThrow(() -> new NotFoundException(
                "LEARNING_PATH_VERSION_NOT_FOUND",
                "Learning path version or completion policy not found"));
    }
}
