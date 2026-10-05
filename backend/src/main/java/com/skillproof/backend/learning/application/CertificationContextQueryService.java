package com.skillproof.backend.learning.application;

import com.skillproof.backend.common.exception.NotFoundException;
import com.skillproof.backend.learning.contract.CertificationContextQuery;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class CertificationContextQueryService
    implements CertificationContextQuery
{

    private final LearningContextReader contextReader;

    public CertificationContextQueryService(
        LearningContextReader contextReader
    ) {
        this.contextReader = contextReader;
    }

    @Override
    public java.util.List<PublishedSource> publishedSources(
        UUID organizationId
    ) {
        return contextReader.publishedSources(organizationId);
    }

    @Override
    public CertificationContext requireCertificationContext(
        UUID learningPathVersionId
    ) {
        return contextReader
            .certification(learningPathVersionId)
            .orElseThrow(() ->
                new NotFoundException(
                    "LEARNING_PATH_VERSION_NOT_FOUND",
                    "Learning path version or completion policy not found"
                )
            );
    }
}
