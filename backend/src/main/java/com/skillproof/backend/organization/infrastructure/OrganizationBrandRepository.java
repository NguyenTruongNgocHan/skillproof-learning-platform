package com.skillproof.backend.organization.infrastructure;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationBrandRepository extends JpaRepository<OrganizationBrand, UUID> {}
