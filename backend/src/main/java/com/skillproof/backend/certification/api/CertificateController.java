package com.skillproof.backend.certification.api;

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

import com.skillproof.backend.certification.application.CertificateService;
import com.skillproof.backend.certification.domain.Certificate;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class CertificateController {

    private final CertificateService certificateService;

    public CertificateController(CertificateService certificateService) {
        this.certificateService = certificateService;
    }

    public record CertificateResponse(
            UUID id,
            UUID certificationProgramId,
            UUID learnerUserId,
            String serialNumber,
            Certificate.Status status,
            java.time.Instant issuedAt,
            java.time.Instant revokedAt,
            String revocationReason,
            UUID organizationId,
            String programName,
            UUID courseVersionId,
            String issuerName,
            String learnerEmail
            ) {

        static CertificateResponse from(Certificate c) {
            return new CertificateResponse(
                    c.id(),
                    c.certificationProgramId(),
                    c.learnerUserId(),
                    c.serialNumber(),
                    c.status(),
                    c.issuedAt(),
                    c.revokedAt(),
                    c.revocationReason(),
                    c.organizationId(),
                    c.programName(),
                    c.courseVersionId(),
                    c.issuerName(),
                    c.learnerEmail()
            );
        }
    }

    @GetMapping("/organizations/{organizationId}/certificates")
    public java.util.List<CertificateResponse> certificates(
            Authentication authentication,
            @PathVariable UUID organizationId
    ) {
        return certificateService
                .listCertificates(
                        authenticatedUserId(authentication),
                        organizationId
                )
                .stream()
                .map(CertificateResponse::from)
                .toList();
    }

    @GetMapping("/organizations/{organizationId}/certificates/search")
    public org.springframework.data.domain.Page<CertificateResponse> searchCertificates(
            Authentication authentication,
            @PathVariable UUID organizationId,
            @org.springframework.web.bind.annotation.RequestParam(
                    required = false
            ) Certificate.Status status,
            @org.springframework.web.bind.annotation.RequestParam(
                    required = false
            ) UUID learnerId,
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
        return certificateService
                .searchCertificates(
                        authenticatedUserId(authentication),
                        organizationId,
                        status,
                        learnerId,
                        query,
                        page,
                        size
                )
                .map(CertificateResponse::from);
    }

    @GetMapping("/certificates/{certificateId}")
    public CertificateResponse certificate(
            Authentication authentication,
            @PathVariable UUID certificateId
    ) {
        return CertificateResponse.from(
                certificateService.certificate(
                        authenticatedUserId(authentication),
                        certificateId
                )
        );
    }

    public record RevokeRequest(
            @jakarta.validation.constraints.NotBlank
            @jakarta.validation.constraints.Size(max = 1000)
            String reason
            ) {

    }

    @PostMapping("/certification-eligibility/{eligibilityId}/certificate")
    @ResponseStatus(HttpStatus.CREATED)
    public CertificateResponse issue(
            Authentication authentication,
            @PathVariable UUID eligibilityId
    ) {
        return CertificateResponse.from(
                certificateService.issue(
                        authenticatedUserId(authentication),
                        eligibilityId
                )
        );
    }

    @PostMapping("/certificates/{certificateId}/revoke")
    public CertificateResponse revoke(
            Authentication authentication,
            @PathVariable UUID certificateId,
            @Valid @RequestBody RevokeRequest request
    ) {
        return CertificateResponse.from(
                certificateService.revoke(
                        authenticatedUserId(authentication),
                        certificateId,
                        request.reason()
                )
        );
    }

    @GetMapping("/public/certificates/{serialNumber}")
    public PublicCertificateResponse verify(@PathVariable String serialNumber) {
        var certificate = certificateService.verify(serialNumber);
        return new PublicCertificateResponse(
                certificate.serialNumber(),
                certificate.status(),
                certificate.issuedAt(),
                certificate.revokedAt(),
                certificate.issuerName(),
                certificate.programName(),
                certificate.courseVersionId(),
                "NOT_ANCHORED"
        );
    }

    public record PublicCertificateResponse(
            String serialNumber,
            Certificate.Status status,
            java.time.Instant issuedAt,
            java.time.Instant revokedAt,
            String issuerName,
            String programName,
            UUID courseVersionId,
            String proofStatus
            ) {

    }

    private UUID authenticatedUserId(Authentication authentication) {
        return (UUID) authentication.getPrincipal();
    }

    @GetMapping("/me/certificates")
    public java.util.List<CertificateResponse> mine(Authentication auth) {
        return certificateService.mine(authenticatedUserId(auth)).stream().map(CertificateResponse::from).toList();
    }

}
