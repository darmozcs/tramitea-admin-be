package com.darmoz.tramiteaadmin.integration;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/** El Swagger es lo que se le entrega al front: público y con candado en todo menos el login. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OpenApiIT extends AbstractIntegrationTest {

    @Test
    void apiDocsArePublicAndListTheEndpoints() throws Exception {
        ResponseEntity<String> response = call(HttpMethod.GET, "/v3/api-docs", null, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        // Copia para entregar sin levantar el servicio (no se versiona: target/).
        Files.writeString(Path.of("target", "openapi.json"),
                objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(body(response)));
        JsonNode docs = body(response);
        assertThat(docs.get("paths").has("/categories/{id}")).isTrue();
        assertThat(docs.get("paths").has("/stats")).isTrue();
        assertThat(docs.get("servers").get(0).get("url").asText()).isEqualTo("/tramitea-admin-api");
    }

    @Test
    void onlyLoginIsWithoutBearer() throws Exception {
        JsonNode paths = body(call(HttpMethod.GET, "/v3/api-docs", null, null)).get("paths");

        assertThat(paths.get("/auth/login").get("post").has("security")).isFalse();
        assertThat(paths.get("/procedures").get("post").has("security")).isTrue();
    }

    @Test
    void swaggerUiIsPublic() {
        assertThat(call(HttpMethod.GET, "/swagger-ui/index.html", null, null).getStatusCode())
                .isEqualTo(HttpStatus.OK);
    }
}
