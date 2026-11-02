package com.darmoz.tramiteaadmin.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Postgres de Testcontainers arrancado una sola vez para todas las *IT (ver
 * la nota en AbstractIntegrationTest de tramitea-be). darmoz-auth se simula
 * firmando tokens con un par RSA generado para el test, con el mismo shape de
 * claims que su JwtService.
 */
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    static final String APPLICATION_ID = "33333333-3333-4333-8333-333333333333";
    static final String ADMIN_ROLE = "ADMIN";

    static final PrivateKey DARMOZ_PRIVATE_KEY;
    static final Path DARMOZ_PUBLIC_KEY_PATH;

    static {
        POSTGRES.start();
        try {
            KeyPair keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();
            DARMOZ_PRIVATE_KEY = keyPair.getPrivate();
            DARMOZ_PUBLIC_KEY_PATH = Files.createTempDirectory("tramitea-admin-keys").resolve("public_key.pem");
            String base64 = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
            Files.writeString(DARMOZ_PUBLIC_KEY_PATH,
                    "-----BEGIN PUBLIC KEY-----\n" + base64 + "\n-----END PUBLIC KEY-----\n");
        } catch (NoSuchAlgorithmException | IOException e) {
            throw new IllegalStateException("No se pudieron generar las llaves RSA de test", e);
        }
    }

    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    protected ObjectMapper objectMapper;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("darmoz-auth.public-key-path", DARMOZ_PUBLIC_KEY_PATH::toString);
        registry.add("darmoz-auth.application-id", () -> APPLICATION_ID);
        registry.add("darmoz-auth.admin-role", () -> ADMIN_ROLE);
    }

    protected static String adminToken() {
        return token(List.of("USER", ADMIN_ROLE), APPLICATION_ID, DARMOZ_PRIVATE_KEY);
    }

    protected static String token(List<String> roles, String applicationId, PrivateKey key) {
        Instant now = Instant.now();
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(UUID.randomUUID().toString())
                .claim("email", "admin@example.com")
                .claim("roles", roles)
                .claim("permissions", List.of())
                .claim("applicationId", applicationId)
                .claim("typ", "access")
                .issuer("darmoz-auth")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(600)))
                .signWith(key, Jwts.SIG.RS256)
                .compact();
    }

    protected ResponseEntity<String> call(HttpMethod method, String path, Object body, String bearerToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (bearerToken != null) {
            headers.setBearerAuth(bearerToken);
        }
        return restTemplate.exchange(path, method, new HttpEntity<>(body, headers), String.class);
    }

    protected ResponseEntity<String> asAdmin(HttpMethod method, String path, Object body) {
        return call(method, path, body, adminToken());
    }

    /** Body JSON de una respuesta 2xx (falla mostrando status y body si no lo es). */
    protected JsonNode body(ResponseEntity<String> response) throws Exception {
        assertThat(response.getStatusCode().is2xxSuccessful())
                .as("%s -> %s", response.getStatusCode(), response.getBody()).isTrue();
        return objectMapper.readTree(response.getBody());
    }

    protected String errorCode(ResponseEntity<String> response) throws Exception {
        return objectMapper.readTree(response.getBody()).get("error").asText();
    }
}
