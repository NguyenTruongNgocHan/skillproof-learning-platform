package com.skillproof.backend.freeze;

import static org.assertj.core.api.Assertions.*;

import com.skillproof.backend.identity.IdentityTestMailConfig;
import java.net.URI;
import java.net.http.*;
import java.nio.file.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"springdoc.api-docs.enabled=true"})
@ActiveProfiles("test")
@Import(IdentityTestMailConfig.class)
class FreezeOpenApiContractTest {
  @Value("${local.server.port}")
  int port;

  @Test
  void runningApplicationExportsTheFrozenHttpContract() throws Exception {
    var client = HttpClient.newHttpClient();
    var response =
        client.send(
            HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/v3/api-docs"))
                .GET()
                .build(),
            HttpResponse.BodyHandlers.ofString());
    assertThat(response.statusCode()).isEqualTo(200);
    var json = new ObjectMapper().readTree(response.body());
    assertThat(json.path("paths").has("/api/v1/courses/versions/{id}/metadata")).isTrue();
    assertThat(json.path("paths").has("/api/v1/quiz/assessments/{id}/reopen")).isTrue();
    assertThat(json.path("paths").has("/api/v1/payments/me/orders/search")).isTrue();
    Files.createDirectories(Path.of("target"));
    Files.writeString(Path.of("target/freeze-openapi.json"), response.body());
  }
}
