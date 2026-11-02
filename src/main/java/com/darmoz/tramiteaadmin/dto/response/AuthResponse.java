package com.darmoz.tramiteaadmin.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Set;
import java.util.UUID;

/** Subconjunto de la respuesta de darmoz-auth (login y refresh) que necesita el admin. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AuthResponse(UUID userId, String email, Set<String> roles, String accessToken, String refreshToken,
                           String tokenType, long expiresIn) {
}
