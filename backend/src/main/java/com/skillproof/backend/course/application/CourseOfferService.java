package com.skillproof.backend.course.application;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.skillproof.backend.commerce.contract.ProductOfferQuery;
import com.skillproof.backend.commerce.domain.ProductType;
import com.skillproof.backend.common.exception.NotFoundException;
import com.skillproof.backend.course.infrastructure.CourseContextRepository;
import com.skillproof.backend.organization.contract.OrganizationPublicQuery;

@Service
public class CourseOfferService implements com.skillproof.backend.course.contract.CourseOfferQuery {

    private final com.skillproof.backend.certification.contract.CertificationOfferingQuery certifications;
    private final CourseContextRepository versions;
    private final OrganizationPublicQuery organizations;

    public CourseOfferService(CourseContextRepository versions, OrganizationPublicQuery organizations, com.skillproof.backend.certification.contract.CertificationOfferingQuery certifications) {
        this.certifications = certifications;
        this.versions = versions;
        this.organizations = organizations;
    }

    public ProductOfferQuery.Offer offer(ProductType type, UUID versionId) {
        var version = versions.findById(versionId).orElseThrow(() -> new NotFoundException("VERSION_NOT_FOUND", "Course version not found"));
        var course = versions.findCourseByVersionId(versionId).orElseThrow();
        boolean organizationReady = course.getOrganizationId() == null || organizations.find(course.getOrganizationId()).map(OrganizationPublicQuery.OrganizationView::approved).orElse(false);
        boolean available = !"DRAFT".equals(version.getStatus()) && organizationReady;
        if (type == ProductType.CERTIFICATION && (course.getOrganizationId() == null || !certifications.activeForVersion(versionId))) {
            available = false;
        }
        return new ProductOfferQuery.Offer(type, versionId, course.getOrganizationId(), version.getPublishedTitle(),
                type == ProductType.CERTIFICATION ? version.getCertificationPriceVnd() : version.getPriceVnd(),
                "PUBLIC".equals(version.getAccessMode()), available);
    }
}
