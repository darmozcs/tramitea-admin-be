package com.darmoz.tramiteaadmin.integration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import java.security.KeyPairGenerator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Solo pasa un token de darmoz-auth válido, de la Application de Tramitea y con el rol admin. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SecurityIT extends AbstractIntegrationTest {

    @Test
    void withoutTokenIs401() {
        assertThat(call(HttpMethod.GET, "/stats", null, null).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void citizenWithoutAdminRoleIs403() {
        String citizen = token(List.of("USER"), APPLICATION_ID, DARMOZ_PRIVATE_KEY);

        assertThat(call(HttpMethod.GET, "/stats", null, citizen).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(call(HttpMethod.DELETE, "/categories/aaaaaaaa-aaaa-4aaa-8aaa-000000000001", null, citizen)
                .getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void adminOfAnotherApplicationIs401() {
        String other = token(List.of(ADMIN_ROLE), "44444444-4444-4444-8444-444444444444", DARMOZ_PRIVATE_KEY);

        assertThat(call(HttpMethod.GET, "/stats", null, other).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void tokenSignedWithAnotherKeyIs401() throws Exception {
        var foreignKey = KeyPairGenerator.getInstance("RSA").generateKeyPair().getPrivate();
        String forged = token(List.of(ADMIN_ROLE), APPLICATION_ID, foreignKey);

        assertThat(call(HttpMethod.GET, "/stats", null, forged).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void adminIs200() {
        assertThat(asAdmin(HttpMethod.GET, "/stats", null).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void healthIsPublic() {
        assertThat(call(HttpMethod.GET, "/actuator/health", null, null).getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
