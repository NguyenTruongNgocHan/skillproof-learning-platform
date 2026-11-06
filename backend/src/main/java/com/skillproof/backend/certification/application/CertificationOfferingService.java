package com.skillproof.backend.certification.application;

import com.skillproof.backend.certification.contract.CertificationOfferingQuery;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class CertificationOfferingService implements CertificationOfferingQuery {

  private final CertificationProgramRepository programs;

  public CertificationOfferingService(CertificationProgramRepository programs) {
    this.programs = programs;
  }

  @Override
  public boolean activeForVersion(UUID version) {
    return programs.activeForVersion(version);
  }

  @Override
  public java.util.Optional<Offering> offeringForVersion(UUID version) {
    return programs.findActiveForVersion(version).map(p -> new Offering(p.id(), p.name()));
  }
}
