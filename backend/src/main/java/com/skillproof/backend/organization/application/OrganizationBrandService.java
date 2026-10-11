package com.skillproof.backend.organization.application;

import com.skillproof.backend.common.exception.NotFoundException;
import com.skillproof.backend.media.contract.ArtworkFiles;
import com.skillproof.backend.operations.contract.ActivityJournal;
import com.skillproof.backend.organization.contract.OrganizationPublicQuery;
import com.skillproof.backend.organization.infrastructure.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class OrganizationBrandService {
    private final OrganizationBrandRepository brands;
    private final OrganizationRepository organizations;
    private final OrganizationEvidenceService access;
    private final OrganizationPublicQuery publicQuery;
    private final ArtworkFiles files;
    private final ActivityJournal journal;

    public OrganizationBrandService(
            OrganizationBrandRepository brands,
            OrganizationRepository organizations,
            OrganizationEvidenceService access,
            OrganizationPublicQuery publicQuery,
            ArtworkFiles files,
            ActivityJournal journal) {
        this.brands = brands;
        this.organizations = organizations;
        this.access = access;
        this.publicQuery = publicQuery;
        this.files = files;
        this.journal = journal;
    }

    public Map<String, Object> read(UUID actor, UUID org) {
        access.requireRead(actor, org);
        return info(org);
    }

    public Map<String, Object> info(UUID org) {
        Map<String, Object> r = new LinkedHashMap<>();
        brands.findById(org)
                .ifPresent(
                        b -> {
                            r.put("logoMediaId", b.getLogoMediaId());
                            r.put("coverMediaId", b.getCoverMediaId());
                            r.put("updatedAt", b.getUpdatedAt());
                        });
        return r;
    }

    @Transactional
    public Map<String, Object> update(UUID actor, UUID org, UUID logo, UUID cover) {
        publicQuery.requireApprovedForUpdate(org);
        access.requireRead(actor, org);
        if (logo != null) files.requireImage(logo, "BRAND", org);
        if (cover != null) files.requireImage(cover, "BRAND", org);
        var b = brands.findById(org).orElseGet(() -> new OrganizationBrand(org));
        b.update(logo, cover);
        brands.save(b);
        journal.record(actor, org, "BRAND_UPDATED", org, "Logo / cover updated");
        return info(org);
    }

    public Map<String, Object> publicProfile(UUID org) {
        var o =
                publicQuery
                        .find(org)
                        .filter(OrganizationPublicQuery.OrganizationView::approved)
                        .orElseThrow(
                                () ->
                                        new NotFoundException(
                                                "ORGANIZATION_NOT_FOUND",
                                                "Organization unavailable"));
        var profile = organizations.find(org).orElseThrow();
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("id", org);
        r.put("displayName", o.displayName());
        r.put("legalName", profile.legalName());
        r.put("industry", profile.industry());
        r.put("country", profile.country());
        r.put("website", profile.website());
        r.put("status", "APPROVED");
        r.put("branding", info(org));
        return r;
    }

    public ArtworkFiles.Image image(UUID org, String kind) {
        publicProfile(org);
        var b =
                brands.findById(org)
                        .orElseThrow(() -> new NotFoundException("IMAGE_NOT_FOUND", "No artwork"));
        UUID id =
                "logo".equals(kind)
                        ? b.getLogoMediaId()
                        : "cover".equals(kind) ? b.getCoverMediaId() : null;
        if (id == null) throw new NotFoundException("IMAGE_NOT_FOUND", "No artwork");
        return files.image(id);
    }
}
