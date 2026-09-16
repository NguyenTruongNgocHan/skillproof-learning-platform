package com.skillproof.backend.identity;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration(proxyBeanMethods = false)
class IdentityTestMailConfig {

    @Bean
    @Primary
    CapturingVerificationEmailSender
    capturingVerificationEmailSender() {

        return new CapturingVerificationEmailSender();
    }
}