package com.darmoz.tramiteaadmin.service;

import com.darmoz.tramiteaadmin.config.DarmozAuthProperties;
import com.darmoz.tramiteaadmin.dto.response.AuthResponse;
import com.darmoz.tramiteaadmin.exception.ApiException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.time.Duration;
import java.util.Map;

/** Llamadas a la API pública de darmoz-auth (/login, /refresh, /logout) con el API_ID de Tramitea. */
@Component
public class DarmozAuthClient {

    private static final Logger log = LoggerFactory.getLogger(DarmozAuthClient.class);
    private static final String API_ID_HEADER = "API_ID";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public DarmozAuthClient(DarmozAuthProperties properties, RestClient.Builder builder, ObjectMapper objectMapper) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(5));
        requestFactory.setReadTimeout(Duration.ofSeconds(10));
        this.restClient = builder
                .baseUrl(properties.getUrl())
                .requestFactory(requestFactory)
                .defaultHeader(API_ID_HEADER, properties.getApplicationId())
                .build();
        this.objectMapper = objectMapper;
    }

    public AuthResponse login(String email, String password) {
        return post("/login", Map.of("email", email, "password", password), null, AuthResponse.class);
    }

    public AuthResponse refresh(String refreshToken) {
        return post("/refresh", Map.of("refreshToken", refreshToken), null, AuthResponse.class);
    }

    /** Revoca el refresh token y, si viene, bloquea el access token. */
    public void logout(String refreshToken, String accessToken) {
        post("/logout", Map.of("refreshToken", refreshToken), accessToken, Void.class);
    }

    private <T> T post(String path, Object body, String bearerToken, Class<T> responseType) {
        try {
            return restClient.post()
                    .uri(path)
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(headers -> {
                        if (bearerToken != null) {
                            headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken);
                        }
                    })
                    .body(body)
                    .retrieve()
                    .body(responseType);
        } catch (RestClientResponseException e) {
            throw translate(path, e);
        } catch (RestClientException e) {
            log.warn("darmoz-auth no responde en {}: {}", path, e.getMessage());
            throw new ApiException(HttpStatus.BAD_GATEWAY, ApiException.AUTH_UNAVAILABLE,
                    "El servicio de autenticación no está disponible");
        }
    }

    private ApiException translate(String path, RestClientResponseException e) {
        int status = e.getStatusCode().value();
        if (status == HttpStatus.UNAUTHORIZED.value()) {
            String code = "/login".equals(path) ? ApiException.INVALID_CREDENTIALS : ApiException.INVALID_SESSION;
            return new ApiException(HttpStatus.UNAUTHORIZED, code, darmozMessage(e, "Credenciales inválidas"));
        }
        if (status == HttpStatus.FORBIDDEN.value()) {
            return new ApiException(HttpStatus.FORBIDDEN, ApiException.EMAIL_NOT_VERIFIED,
                    darmozMessage(e, "El email no está verificado"));
        }
        if (status == HttpStatus.BAD_REQUEST.value()) {
            return ApiException.badRequest(darmozMessage(e, "Request invalido"));
        }
        log.warn("darmoz-auth respondió {} en {}", status, path);
        return new ApiException(HttpStatus.BAD_GATEWAY, ApiException.AUTH_UNAVAILABLE,
                "El servicio de autenticación no está disponible");
    }

    private String darmozMessage(RestClientResponseException e, String fallback) {
        try {
            JsonNode message = objectMapper.readTree(e.getResponseBodyAsString()).get("message");
            return message == null || message.isNull() ? fallback : message.asText();
        } catch (Exception ignored) {
            return fallback;
        }
    }
}
