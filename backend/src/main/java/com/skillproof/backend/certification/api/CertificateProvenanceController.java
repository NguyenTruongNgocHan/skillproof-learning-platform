package com.skillproof.backend.certification.api;

import com.skillproof.backend.certification.application.CertificateProvenanceService;

import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/public/certificates")
public class CertificateProvenanceController {
    private final CertificateProvenanceService service;

    public CertificateProvenanceController(CertificateProvenanceService service) {
        this.service = service;
    }

    @GetMapping("/{serial}/provenance")
    public Map<String, Object> get(@PathVariable String serial) {
        return service.publicProof(serial);
    }
}
