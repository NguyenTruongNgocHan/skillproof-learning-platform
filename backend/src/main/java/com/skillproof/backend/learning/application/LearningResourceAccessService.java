package com.skillproof.backend.learning.application;

import com.skillproof.backend.common.exception.BadRequestException;
import com.skillproof.backend.common.exception.NotFoundException;
import com.skillproof.backend.learning.contract.LearningResourceAccessQuery;
import com.skillproof.backend.organization.contract.OrganizationAuthorityQuery;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

/**
 * Learning-owned implementation of the resource access contract exposed to
 * Media.
 */
@Service
public class LearningResourceAccessService
    implements LearningResourceAccessQuery
{

    private final LearningContextReader contextReader;
    private final com.skillproof.backend.learning.infrastructure.LearningContextRepository versions;
    private final LearningAccess learningAccess;
    private final OrganizationAuthorityQuery organizations;

    public LearningResourceAccessService(
        LearningContextReader contextReader,
        LearningAccess learningAccess,
        OrganizationAuthorityQuery organizations,
        com.skillproof.backend.learning.infrastructure.LearningContextRepository versions
    ) {
        this.contextReader = contextReader;
        this.learningAccess = learningAccess;
        this.organizations = organizations;
        this.versions = versions;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void requireDraftManage(UUID actorId, UUID resourceId) {
        var resource = resource(resourceId);
        UUID organizationId = resource.organizationId();
        learningAccess.organizer(actorId, organizationId);
        var locked = versions.findForUpdate(resource.versionId()).orElseThrow();
        if (!"DRAFT".equals(locked.getStatus())) {
            throw new BadRequestException(
                "VERSION_IMMUTABLE",
                "Upload to a draft version"
            );
        }
    }

    @Override
    public void requireRead(UUID actorId, UUID resourceId) {
        var resource = resource(resourceId);
        UUID organizationId = resource.organizationId();
        if (
            organizations.hasAuthority(
                organizationId,
                actorId,
                "MANAGE_CONTENT"
            )
        ) {
            learningAccess.organizer(actorId, organizationId);
            return;
        }

        learningAccess.learner(actorId);
        if (!contextReader.hasEnrollment(actorId, resource.versionId())) {
            throw new AccessDeniedException(
                "Enrollment required for this version"
            );
        }
    }

    @Override
    public String resourceKind(UUID resourceId) {
        return resource(resourceId).kind();
    }

    private LearningContextReader.ResourceContext resource(UUID resourceId) {
        return contextReader
            .resource(resourceId)
            .orElseThrow(() ->
                new NotFoundException(
                    "RESOURCE_NOT_FOUND",
                    "Resource not found"
                )
            );
    }
}
