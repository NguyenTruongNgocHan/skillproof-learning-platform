package com.skillproof.backend.learning.application;

import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.skillproof.backend.common.exception.BadRequestException;
import com.skillproof.backend.common.exception.NotFoundException;
import com.skillproof.backend.learning.contract.LearningResourceAccessQuery;
import com.skillproof.backend.organization.contract.OrganizationAuthorityQuery;

/**
 * Learning-owned implementation of the resource access contract exposed to
 * Media.
 */
@Service
public class LearningResourceAccessService implements LearningResourceAccessQuery {

    private final LearningContextReader contextReader;
    private final LearningAccess learningAccess;
    private final OrganizationAuthorityQuery organizations;

    public LearningResourceAccessService(
            LearningContextReader contextReader,
            LearningAccess learningAccess,
            OrganizationAuthorityQuery organizations) {
        this.contextReader = contextReader;
        this.learningAccess = learningAccess;
        this.organizations = organizations;
    }

    @Override
    public void requireDraftManage(UUID actorId, UUID resourceId) {
        var resource = resource(resourceId);
        UUID organizationId = resource.organizationId();
        learningAccess.organizer(actorId, organizationId);
        if (!"DRAFT".equals(resource.status())) {
            throw new BadRequestException("VERSION_IMMUTABLE", "Upload to a draft version");
        }
    }

    @Override
    public void requireRead(UUID actorId, UUID resourceId) {
        var resource = resource(resourceId);
        UUID organizationId = resource.organizationId();
        if (organizations.hasAuthority(organizationId, actorId, "MANAGE_CONTENT")) {
            learningAccess.organizer(actorId, organizationId);
            return;
        }

        learningAccess.learner(actorId);
        if (!contextReader.hasEnrollment(actorId, resource.versionId())) {
            throw new AccessDeniedException("Enrollment required for this version");
        }
    }

    @Override
    public String resourceKind(UUID resourceId) {
        return resource(resourceId).kind();
    }

    private LearningContextReader.ResourceContext resource(UUID resourceId) {
        return contextReader.resource(resourceId).orElseThrow(
                () -> new NotFoundException("RESOURCE_NOT_FOUND", "Resource not found"));
    }
}
