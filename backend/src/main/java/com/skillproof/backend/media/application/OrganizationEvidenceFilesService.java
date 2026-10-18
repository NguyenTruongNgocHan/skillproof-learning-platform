package com.skillproof.backend.media.application;

import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.skillproof.backend.common.exception.BadRequestException;
import com.skillproof.backend.media.contract.OrganizationEvidenceFiles;
import com.skillproof.backend.media.infrastructure.persistence.MediaAssetRepository;

@Service
public class OrganizationEvidenceFilesService implements OrganizationEvidenceFiles {
    private final MediaAssetRepository files;

    public OrganizationEvidenceFilesService(MediaAssetRepository files) {
        this.files = files;
    }

    @Override
    public void requireImage(UUID org, UUID id) {
        var file = files.findById(id).orElseThrow(() ->
                new BadRequestException("COMPANY_IMAGE_INVALID", "Upload a company image first"));
        if (!org.equals(file.getOrganizationId())
                || !"ORGANIZATION".equals(file.getScope())
                || !Set.of("image/jpeg", "image/png", "image/webp").contains(file.getMimeType()))
            throw new BadRequestException("COMPANY_IMAGE_INVALID", "A company image must be a JPEG, PNG or WebP organization upload");
    }

    public void requireFile(UUID org, UUID id, String purpose) {
        var file =
                files.findById(id)
                        .orElseThrow(
                                () ->
                                        new BadRequestException(
                                                "EVIDENCE_FILE", "Upload a document first"));
        String scope = "ISSUER".equals(purpose) ? "ISSUER" : "ORGANIZATION";
        if (!org.equals(file.getOrganizationId())
                || !scope.equals(file.getScope())
                || (!file.getMimeType().startsWith("image/")
                        && !"application/pdf".equals(file.getMimeType())))
            throw new BadRequestException("EVIDENCE_FILE", "Wrong document scope or organization");
    }
}
