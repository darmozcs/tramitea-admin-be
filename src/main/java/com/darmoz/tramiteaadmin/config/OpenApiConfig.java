package com.darmoz.tramiteaadmin.config;

import com.darmoz.tramiteaadmin.controller.AuthController;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger UI en /tramitea-admin-api/swagger-ui.html y el JSON en
 * /tramitea-admin-api/v3/api-docs (se apagan con API_DOCS_ENABLED=false).
 */
@Configuration
public class OpenApiConfig {

    static final String BEARER = "bearer";

    @Bean
    public OpenAPI tramiteaAdminOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Tramitea Admin API")
                        .version("v1")
                        .description("""
                                Backend del panel de administración de Tramitea: ABM de trámites, categorías e \
                                instituciones, sugerencias y métricas. Usa la misma base que la API pública \
                                (tramitea-be), así que los cambios se ven en la web al instante.

                                Autenticación: `POST /auth/login` (credenciales de darmoz-auth, Application \
                                Tramitea). Solo entran usuarios con el rol admin. El resto de los endpoints lleva \
                                `Authorization: Bearer <accessToken>`: sin token 401, sin el rol 403.

                                Errores: `{ timestamp, status, error, message }`; `error` lleva un código estable \
                                (`not-admin`, `duplicated`, `in-use`, `invalid-payload`...). Fechas ISO-8601 en \
                                UTC; los campos null se omiten."""))
                // Relativo: Swagger UI lo resuelve contra el host desde el que se abre.
                .servers(List.of(new Server().url("/tramitea-admin-api")))
                .components(new Components().addSecuritySchemes(BEARER, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
    }

    /** Candado en todo menos el login. */
    @Bean
    public OperationCustomizer bearerOutsideAuth() {
        return (operation, handlerMethod) -> {
            if (!AuthController.class.equals(handlerMethod.getBeanType())) {
                operation.addSecurityItem(new SecurityRequirement().addList(BEARER));
            }
            return operation;
        };
    }
}
