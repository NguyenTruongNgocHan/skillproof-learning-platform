package com.skillproof.backend.freeze;

import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles(
    profiles = {"test", "postgres-test"},
    inheritProfiles = false)
@EnabledIfEnvironmentVariable(named = "POSTGRES_IT_ENABLED", matches = "true")
class PaymentFreezePostgresIntegrationTest extends PaymentFreezeIntegrationTest {}
