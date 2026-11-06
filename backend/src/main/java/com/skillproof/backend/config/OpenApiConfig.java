package com.skillproof.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI skillProofOpenApi() {
    return new OpenAPI()
        .info(
            new Info()
                .title("SkillProof API")
                .description(
                    """
                    REST API for the SkillProof Course Platform.

                    Main domains:
                    - Identity
                    - Organization
                    - Course
                    - Quiz & Assessment
                    - Assignment and rubric grading
                    - Certification
                    - Verification
                    - Library resources and moderation
                    - Marketplace & Access
                    - Administration
                    """)
                .version("1.1.0-freeze")
                .contact(new Contact().name("SkillProof")))
        .components(
            new Components()
                .addSecuritySchemes(
                    "bearerAuth",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
  }
}
