package com.skillproof.backend.organization.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrganizationBrandRepository extends JpaRepository<OrganizationBrand, UUID> {}
