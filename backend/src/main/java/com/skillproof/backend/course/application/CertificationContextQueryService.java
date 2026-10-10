package com.skillproof.backend.course.application;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.skillproof.backend.common.exception.NotFoundException;
import com.skillproof.backend.course.contract.CertificationContextQuery;

@Service
public class CertificationContextQueryService
        implements CertificationContextQuery {

    private final CourseContextReader contextReader;

    public CertificationContextQueryService(
            CourseContextReader contextReader
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
            UUID courseVersionId
    ) {
        return contextReader
                .certification(courseVersionId)
                .orElseThrow(()
                        -> new NotFoundException(
                        "COURSE_PATH_VERSION_NOT_FOUND",
                        "Course path version or completion policy not found"
                )
                );
    }
}
