package com.skillproof.backend.certification.application;

import com.skillproof.backend.feedback.contract.FeedbackSubjectProvider;
import java.util.*;
import org.springframework.stereotype.Service;

@Service
public class CertificateFeedbackSubject implements FeedbackSubjectProvider {
  private final CertificateRepository certificates;

  public CertificateFeedbackSubject(CertificateRepository certificates) {
    this.certificates = certificates;
  }

  public String type() {
    return "CERTIFICATE";
  }

  public Subject subject(UUID target, UUID actor) {
    var c = certificates.findById(target).orElseThrow();
    return new Subject(
        c.organizationId(),
        null,
        actor != null && actor.equals(c.learnerUserId()),
        c.programName());
  }
}
