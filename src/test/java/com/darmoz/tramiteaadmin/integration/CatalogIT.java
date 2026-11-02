package com.darmoz.tramiteaadmin.integration;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/** ABM contra el esquema y el catálogo sembrado de tramitea-be (V1 + V2). */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CatalogIT extends AbstractIntegrationTest {

    static final String IDENTITY_CATEGORY_ID = "aaaaaaaa-aaaa-4aaa-8aaa-000000000001";
    static final String DNIC_INSTITUTION_ID = "bbbbbbbb-bbbb-4bbb-8bbb-000000000001";
    static final String CEDULA_ID = "cccccccc-cccc-4ccc-8ccc-000000000001";

    @Test
    void categoryLifecycle() throws Exception {
        String code = "TEST_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Map<String, Object> payload = Map.of("code", code, "name", "Categoría de prueba", "description", "",
                "icon", "category", "displayOrder", 99, "isActive", false);

        ResponseEntity<String> created = asAdmin(HttpMethod.POST, "/categories", payload);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        JsonNode category = body(created);
        String id = category.get("id").asText();
        assertThat(category.has("description")).isFalse();
        assertThat(category.get("isActive").asBoolean()).isFalse();

        // Duplicado sin importar mayúsculas.
        ResponseEntity<String> duplicated = asAdmin(HttpMethod.POST, "/categories",
                Map.of("code", code.toLowerCase(), "name", "Otra", "icon", "x", "displayOrder", 1, "isActive", true));
        assertThat(duplicated.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(errorCode(duplicated)).isEqualTo("duplicated");

        // Las inactivas también se listan (tramitea-be las oculta).
        JsonNode inactive = body(asAdmin(HttpMethod.GET, "/categories?isActive=false&search=" + code, null));
        assertThat(inactive).hasSize(1);

        Map<String, Object> update = new HashMap<>(payload);
        update.put("name", "Renombrada");
        update.put("isActive", true);
        JsonNode updated = body(asAdmin(HttpMethod.PUT, "/categories/" + id, update));
        assertThat(updated.get("name").asText()).isEqualTo("Renombrada");

        assertThat(asAdmin(HttpMethod.DELETE, "/categories/" + id, null).getStatusCode())
                .isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(asAdmin(HttpMethod.GET, "/categories/" + id, null).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void categoryWithProceduresCannotBeDeleted() throws Exception {
        ResponseEntity<String> response = asAdmin(HttpMethod.DELETE, "/categories/" + IDENTITY_CATEGORY_ID, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(errorCode(response)).isEqualTo("in-use");
    }

    @Test
    void invalidCategoryIs400() throws Exception {
        ResponseEntity<String> response = asAdmin(HttpMethod.POST, "/categories",
                Map.of("code", "", "name", "x", "icon", "x", "displayOrder", 0, "isActive", true));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(errorCode(response)).isEqualTo("invalid-payload");
    }

    @Test
    void institutionLifecycle() throws Exception {
        Map<String, Object> payload = Map.of("acronym", "TST", "name", "Institución de prueba",
                "officialWebsite", "https://example.com");
        JsonNode created = body(asAdmin(HttpMethod.POST, "/institutions", payload));
        String id = created.get("id").asText();
        assertThat(created.get("createdAt").asText()).isNotBlank();

        assertThat(body(asAdmin(HttpMethod.GET, "/institutions?search=prueba", null)))
                .anyMatch(i -> i.get("id").asText().equals(id));

        assertThat(asAdmin(HttpMethod.DELETE, "/institutions/" + DNIC_INSTITUTION_ID, null).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
        assertThat(asAdmin(HttpMethod.DELETE, "/institutions/" + id, null).getStatusCode())
                .isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void procedureLifecycle() throws Exception {
        String name = "Trámite de prueba " + UUID.randomUUID().toString().substring(0, 6);
        Map<String, Object> payload = procedurePayload(name, false);

        ResponseEntity<String> created = asAdmin(HttpMethod.POST, "/procedures", payload);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        JsonNode procedure = body(created);
        String id = procedure.get("id").asText();
        assertThat(procedure.get("slug").get("es").asText()).startsWith("tramite-de-prueba-");
        assertThat(procedure.get("cost").get("currency").asText()).isEqualTo("UYU");
        assertThat(procedure.get("requirements")).hasSize(2);
        assertThat(procedure.get("requirements").get(1).get("displayOrder").asInt()).isEqualTo(2);
        assertThat(procedure.get("steps").get(0).get("comments").get(0).get("level").asText()).isEqualTo("WARNING");
        assertThat(procedure.get("faqs")).hasSize(1);

        // La búsqueda usa searchText (sin tildes), igual que tramitea-be.
        JsonNode page = body(asAdmin(HttpMethod.GET, "/procedures?searchTerm=tramite de prueba&pageSize=100", null));
        assertThat(page.get("items")).anyMatch(p -> p.get("id").asText().equals(id));

        // El mismo slug choca.
        ResponseEntity<String> duplicated = asAdmin(HttpMethod.POST, "/procedures", payload);
        assertThat(duplicated.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

        // Editar reemplaza los hijos; pasar a gratis borra el costo.
        Map<String, Object> update = procedurePayload(name, true);
        update.put("requirements", List.of(Map.of("title", "Único requisito", "isRequired", true,
                "isDigitallyVerifiable", false)));
        update.put("steps", List.of());
        JsonNode updated = body(asAdmin(HttpMethod.PUT, "/procedures/" + id, update));
        assertThat(updated.has("cost")).isFalse();
        assertThat(updated.get("requirements")).hasSize(1);
        assertThat(updated.has("steps") && !updated.get("steps").isEmpty()).isFalse();

        assertThat(asAdmin(HttpMethod.DELETE, "/procedures/" + id, null).getStatusCode())
                .isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(asAdmin(HttpMethod.GET, "/procedures/" + id, null).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void paidProcedureWithoutCostIs400() throws Exception {
        Map<String, Object> payload = procedurePayload("Sin costo " + UUID.randomUUID(), false);
        payload.remove("cost");

        ResponseEntity<String> response = asAdmin(HttpMethod.POST, "/procedures", payload);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void procedureDetailById() throws Exception {
        JsonNode cedula = body(asAdmin(HttpMethod.GET, "/procedures/" + CEDULA_ID, null));

        assertThat(cedula.get("slug").get("es").asText()).isEqualTo("cedula-de-identidad");
        assertThat(cedula.get("displayOrder").asInt()).isEqualTo(1);
    }

    @Test
    void statsAndSuggestions() throws Exception {
        JsonNode stats = body(asAdmin(HttpMethod.GET, "/stats", null));
        assertThat(stats.get("procedures").asLong()).isGreaterThanOrEqualTo(14);
        assertThat(stats.get("categories").asLong()).isGreaterThanOrEqualTo(8);
        assertThat(stats.has("registeredUsers")).isTrue();

        JsonNode suggestions = body(asAdmin(HttpMethod.GET, "/suggestions", null));
        assertThat(suggestions.get("currentPage").asInt()).isEqualTo(1);
        assertThat(suggestions.has("items")).isTrue();
    }

    private static Map<String, Object> procedurePayload(String name, boolean isFree) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", name);
        payload.put("applicationMethod", "HYBRID");
        payload.put("authenticationLevel", "PUBLIC");
        payload.put("group", "IDENTITY");
        payload.put("description", "Descripción de un trámite de prueba.");
        payload.put("imageUrl", "https://example.com/imagen.jpg");
        payload.put("categoryId", IDENTITY_CATEGORY_ID);
        payload.put("institutionId", DNIC_INSTITUTION_ID);
        payload.put("appointmentRequired", true);
        payload.put("isFree", isFree);
        payload.put("cost", Map.of("amount", 1500, "currency", "UYU"));
        payload.put("validity", Map.of("value", "5", "unit", "años"));
        payload.put("displayOrder", 500);
        payload.put("requirements", List.of(
                Map.of("title", "Cédula vigente", "isRequired", true, "isDigitallyVerifiable", false),
                Map.of("title", "Foto", "description", "Carné", "isRequired", false,
                        "isDigitallyVerifiable", false)));
        payload.put("steps", List.of(Map.of("title", "Agendar", "instructions", "Pedir hora en la web",
                "comments", List.of(Map.of("level", "WARNING", "content", "Hay demoras")))));
        payload.put("faqs", List.of(Map.of("question", "¿Cuánto demora?", "answer", "Una semana")));
        return payload;
    }
}
