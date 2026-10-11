package com.skillproof.backend.certification.application;

import com.skillproof.backend.certification.domain.Certificate;
import com.skillproof.backend.certification.infrastructure.*;
import com.skillproof.backend.operations.contract.ActivityJournal;
import com.skillproof.backend.organization.contract.IssuerAuthorizationQuery;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;

@Service
public class CertificateProvenanceService {
    private final CertificateProvenanceRepository proofs;
    private final CertificateRepository certificates;
    private final IssuerAuthorizationQuery issuers;
    private final ObjectMapper json;
    private final ActivityJournal journal;

    public CertificateProvenanceService(
            CertificateProvenanceRepository proofs,
            CertificateRepository certificates,
            IssuerAuthorizationQuery issuers,
            ObjectMapper json,
            ActivityJournal journal) {
        this.proofs = proofs;
        this.certificates = certificates;
        this.issuers = issuers;
        this.json = json;
        this.journal = journal;
    }

    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.MANDATORY)
    public Certificate capture(Certificate c, UUID actor) {
        var a = issuers.authorization(c.organizationId(), c.courseVersionId());
        try {
            var data = new TreeMap<String, Object>();
            data.put("schema", "skillproof.credential.v1");
            data.put("certificateId", c.id().toString());
            data.put("learnerId", c.learnerUserId().toString());
            data.put("serial", c.serialNumber());
            data.put("organizationId", c.organizationId().toString());
            data.put("issuerName", c.issuerName());
            data.put("programName", c.programName());
            data.put("courseVersionId", c.courseVersionId().toString());
            data.put("issuedAt", c.issuedAt().toString());
            data.put("issuerApplicationId", a.id().toString());
            data.put("authorizedScope", a.scope());
            data.put("evidenceIds", a.evidenceIds());
            String payload = json.writeValueAsString(data);
            String hash =
                    HexFormat.of()
                            .formatHex(
                                    MessageDigest.getInstance("SHA-256")
                                            .digest(payload.getBytes(StandardCharsets.UTF_8)));
            proofs.save(
                    new CertificateProvenanceEntity(c.id(), a.id(), hash, payload, Instant.now()));
            journal.notify(
                    c.learnerUserId(),
                    c.organizationId(),
                    "CERTIFICATE",
                    "Your certificate has been issued: " + c.programName(),
                    "/verify/" + c.serialNumber());
            journal.record(actor, c.organizationId(), "CERTIFICATE_ISSUED", c.id(), hash);
            return c;
        } catch (java.security.NoSuchAlgorithmException | tools.jackson.core.JacksonException ex) {
            throw new IllegalStateException("Could not capture credential provenance", ex);
        }
    }

    @Transactional(readOnly = true)
    public Map<String, Object> publicProof(String serial) {
        var c = certificates.findBySerial(serial).orElseThrow();
        var p = proofs.findById(c.id());
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("organizationId", c.organizationId());
        result.put("issuerName", c.issuerName());
        result.put("courseVersionId", c.courseVersionId());
        result.put("credentialStatus", c.status());
        result.put("proofStatus", "NOT_ANCHORED");
        p.ifPresent(
                value -> {
                    result.put("issuerApplicationId", value.getIssuerApplicationId());
                    result.put("credentialHash", value.getCredentialHash());
                    result.put("capturedAt", value.getCapturedAt());
                });
        return result;
    }
}
