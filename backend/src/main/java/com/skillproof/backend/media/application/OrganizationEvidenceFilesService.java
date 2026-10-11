package com.skillproof.backend.media.application;

import com.skillproof.backend.common.exception.BadRequestException;
import com.skillproof.backend.media.contract.OrganizationEvidenceFiles;
import com.skillproof.backend.media.infrastructure.persistence.MediaAssetRepository;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class OrganizationEvidenceFilesService implements OrganizationEvidenceFiles {
    private final MediaAssetRepository files;

    public OrganizationEvidenceFilesService(MediaAssetRepository files) {
        this.files = files;
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
