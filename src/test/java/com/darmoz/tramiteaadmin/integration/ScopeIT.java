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

/**
 * Cada admin administra el país de su ubicación en tr_users (aunque sea una
 * provincia): trámites, instituciones, sugerencias y ubicaciones de otros
 * países no existen para él.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ScopeIT extends AbstractIntegrationTest {

    static final String CEDULA_ID = CatalogIT.CEDULA_ID;
    static final String DNIC_INSTITUTION_ID = CatalogIT.DNIC_INSTITUTION_ID;
    static final String IDENTITY_CATEGORY_ID = CatalogIT.IDENTITY_CATEGORY_ID;

    @Test
    void adminWithoutLocationIs403() throws Exception {
        String noLocation = token(List.of("USER", ADMIN_ROLE), APPLICATION_ID, DARMOZ_PRIVATE_KEY);

        ResponseEntity<String> response = call(HttpMethod.GET, "/procedures", null, noLocation);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(errorCode(response)).isEqualTo("no-location");
    }

    @Test
    void adminOfAnotherCountryDoesNotSeeUruguay() throws Exception {
        String brazil = adminTokenIn("BR-SP");

        assertThat(body(call(HttpMethod.GET, "/procedures", null, brazil)).get("total").asInt()).isZero();
        assertThat(call(HttpMethod.GET, "/procedures/" + CEDULA_ID, null, brazil).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(call(HttpMethod.DELETE, "/procedures/" + CEDULA_ID, null, brazil).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(body(call(HttpMethod.GET, "/institutions", null, brazil)).get("total").asInt()).isZero();
        assertThat(body(call(HttpMethod.GET, "/institutions/catalog", null, brazil))).isEmpty();
        assertThat(body(call(HttpMethod.GET, "/stats", null, brazil)).get("procedures").asInt()).isZero();

        ResponseEntity<String> inUruguay = call(HttpMethod.POST, "/procedures",
                procedurePayload(locationId("UY"), DNIC_INSTITUTION_ID), brazil);
        assertThat(inUruguay.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(errorCode(inUruguay)).isEqualTo("location-not-allowed");
    }

    @Test
    void provinceAdminManagesTheWholeCountry() throws Exception {
        String brazil = adminTokenIn("BR-SP");

        JsonNode institution = body(call(HttpMethod.POST, "/institutions", Map.of("acronym", "RFB",
                "name", "Receita Federal", "officialWebsite", "https://www.gov.br/receitafederal",
                "locationId", locationId("BR")), brazil));
        String institutionId = institution.get("id").asText();

        // Trámite nacional de Brasil y otro de un estado que no es el del admin.
        JsonNode national = body(call(HttpMethod.POST, "/procedures",
                procedurePayload(locationId("BR"), institutionId), brazil));
        JsonNode rio = body(call(HttpMethod.POST, "/procedures",
                procedurePayload(locationId("BR-RJ"), institutionId), brazil));
        assertThat(rio.get("locationId").asText()).isEqualTo(locationId("BR-RJ").toString());

        // Una institución de Uruguay no sirve para un trámite de Brasil.
        assertThat(call(HttpMethod.POST, "/procedures", procedurePayload(locationId("BR"), DNIC_INSTITUTION_ID),
                brazil).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        // El admin de Uruguay no los ve.
        assertThat(asAdmin(HttpMethod.GET, "/procedures/" + national.get("id").asText(), null).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(asAdmin(HttpMethod.GET, "/institutions/" + institutionId, null).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);

        call(HttpMethod.DELETE, "/procedures/" + national.get("id").asText(), null, brazil);
        call(HttpMethod.DELETE, "/procedures/" + rio.get("id").asText(), null, brazil);
        call(HttpMethod.DELETE, "/institutions/" + institutionId, null, brazil);
    }

    @Test
    void locationsAreListedAndToggledOnlyWithinTheCountry() throws Exception {
        JsonNode uruguay = body(asAdmin(HttpMethod.GET, "/locations", null));
        assertThat(uruguay).hasSize(20);
        assertThat(uruguay.get(0).get("code").asText()).isEqualTo("UY");

        JsonNode brazil = body(call(HttpMethod.GET, "/locations", null, adminTokenIn("BR-SP")));
        assertThat(brazil).hasSize(28);
        assertThat(brazil.get(0).get("code").asText()).isEqualTo("BR");
        assertThat(brazil.get(0).get("isActive").asBoolean()).isFalse();

        String flores = "/locations/" + locationId("UY-FS");
        assertThat(body(asAdmin(HttpMethod.PATCH, flores, Map.of("isActive", false))).get("isActive").asBoolean())
                .isFalse();
        assertThat(call(HttpMethod.PATCH, flores, Map.of("isActive", true), adminTokenIn("BR-SP")).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(body(asAdmin(HttpMethod.PATCH, flores, Map.of("isActive", true))).get("isActive").asBoolean())
                .isTrue();
    }

    @Test
    void suggestionsAreReviewedByAdminsOfTheirCountry() throws Exception {
        UUID toAccept = insertSuggestion("UY-MO");
        UUID toReject = insertSuggestion("UY");
        String brazil = adminTokenIn("BR-SP");

        JsonNode pending = body(asAdmin(HttpMethod.GET, "/suggestions?status=PENDING&pageSize=100", null));
        assertThat(pending.get("items")).anyMatch(s -> s.get("id").asText().equals(toAccept.toString()));
        assertThat(body(call(HttpMethod.GET, "/suggestions", null, brazil)).get("total").asInt()).isZero();
        assertThat(call(HttpMethod.POST, "/suggestions/" + toAccept + "/accept",
                procedurePayload(locationId("BR"), DNIC_INSTITUTION_ID), brazil).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);

        Map<String, Object> procedure = procedurePayload(locationId("UY-MO"), DNIC_INSTITUTION_ID);
        JsonNode accepted = body(asAdmin(HttpMethod.POST, "/suggestions/" + toAccept + "/accept", procedure));
        assertThat(accepted.get("status").asText()).isEqualTo("ACCEPTED");
        assertThat(accepted.get("reviewedAt").asText()).isNotBlank();
        String procedureId = accepted.get("procedureId").asText();
        assertThat(body(asAdmin(HttpMethod.GET, "/procedures/" + procedureId, null)).get("locationId").asText())
                .isEqualTo(locationId("UY-MO").toString());

        ResponseEntity<String> again = asAdmin(HttpMethod.POST, "/suggestions/" + toAccept + "/reject", null);
        assertThat(again.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(errorCode(again)).isEqualTo("already-reviewed");

        JsonNode rejected = body(asAdmin(HttpMethod.POST, "/suggestions/" + toReject + "/reject",
                Map.of("reason", "Ya existe")));
        assertThat(rejected.get("status").asText()).isEqualTo("REJECTED");
        assertThat(rejected.get("rejectionReason").asText()).isEqualTo("Ya existe");

        asAdmin(HttpMethod.DELETE, "/procedures/" + procedureId, null);
    }

    private UUID insertSuggestion(String locationCode) {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update("""
                insert into tr_procedure_suggestions (id, title, institution_id, details, location_id, created_at)
                values (?, 'Permiso de pesca', ?::uuid, 'Falta', (select id from tr_locations where code = ?), now())""",
                id, DNIC_INSTITUTION_ID, locationCode);
        return id;
    }

    /** Nombre único: el slug se genera del nombre y tiene que ser único. */
    private static Map<String, Object> procedurePayload(UUID locationId, String institutionId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", "Trámite de alcance " + UUID.randomUUID().toString().substring(0, 8));
        payload.put("applicationMethod", "FULLY_DIGITAL");
        payload.put("authenticationLevel", "PUBLIC");
        payload.put("group", "IDENTITY");
        payload.put("description", "Prueba de alcance por país.");
        payload.put("imageUrl", "https://example.com/imagen.jpg");
        payload.put("categoryId", IDENTITY_CATEGORY_ID);
        payload.put("institutionId", institutionId);
        payload.put("locationId", locationId);
        payload.put("isFree", true);
        return payload;
    }
}
