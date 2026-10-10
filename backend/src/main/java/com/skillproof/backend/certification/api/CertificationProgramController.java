package com.skillproof.backend.certification.api;

import com.skillproof.backend.certification.application.CertificationProgramService;
import com.skillproof.backend.course.contract.EnrollmentLookupQuery;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class CertificationProgramController {

    private final CertificationProgramService certificationProgramService;

    public CertificationProgramController(
            CertificationProgramService certificationProgramService
    ) {
        this.certificationProgramService = certificationProgramService;
    }

    @PostMapping("/organizations/{organizationId}/certification-programs")
    @ResponseStatus(HttpStatus.CREATED)
    public CertificationProgramResponse createProgram(
            Authentication authentication,
            @PathVariable UUID organizationId,
            @Valid @RequestBody CreateCertificationProgramRequest request
    ) {
        var program = certificationProgramService.createProgram(
                authenticatedUserId(authentication),
                organizationId,
                request.courseVersionId(),
                request.name()
        );
        return CertificationProgramResponse.from(program);
    }

    @GetMapping("/organizations/{organizationId}/certification-programs")
    public java.util.List<CertificationProgramResponse> listPrograms(
            Authentication authentication,
            @PathVariable UUID organizationId
    ) {
        return certificationProgramService
                .listPrograms(authenticatedUserId(authentication), organizationId)
                .stream()
                .map(CertificationProgramResponse::from)
                .toList();
    }

    @GetMapping("/certification-programs/{programId}/enrollments")
    public org.springframework.data.domain.Page<EnrollmentLookupQuery.EnrollmentSummary> enrollments(
            Authentication authentication,
            @PathVariable UUID programId,
            @org.springframework.web.bind.annotation.RequestParam(
                    defaultValue = ""
            ) String query,
            @org.springframework.web.bind.annotation.RequestParam(
                    defaultValue = "0"
            ) int page,
            @org.springframework.web.bind.annotation.RequestParam(
                    defaultValue = "20"
            ) int size
    ) {
        return certificationProgramService.searchEnrollments(
                authenticatedUserId(authentication),
                programId,
                query,
                page,
                size
        );
    }

    @GetMapping("/organizations/{id}/certification-program-sources")
    public java.util.List<com.skillproof.backend.course.contract.CertificationContextQuery.PublishedSource> sources(
            Authentication auth,
            @PathVariable UUID id
    ) {
        return certificationProgramService.sources(
                authenticatedUserId(auth),
                id
        );
    }

    @PostMapping("/certification-programs/{id}/retire")
    public CertificationProgramResponse retire(
            Authentication auth,
            @PathVariable UUID id
    ) {
        return CertificationProgramResponse.from(
                certificationProgramService.retire(authenticatedUserId(auth), id)
        );
    }

    private UUID authenticatedUserId(Authentication authentication) {
        return (UUID) authentication.getPrincipal();
    }
}
