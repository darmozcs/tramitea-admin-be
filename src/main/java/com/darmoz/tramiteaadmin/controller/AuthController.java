package com.darmoz.tramiteaadmin.controller;

import com.darmoz.tramiteaadmin.dto.request.AuthRequests;
import com.darmoz.tramiteaadmin.dto.response.AuthResponse;
import com.darmoz.tramiteaadmin.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "Login contra darmoz-auth (Application Tramitea). Solo entran usuarios con el rol admin.")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final String BEARER_PREFIX = "Bearer ";

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "401 `invalid-credentials` si email o password no coinciden; 403 `not-admin` si el usuario no tiene el rol admin (la sesión abierta en darmoz-auth se revoca); 403 `email-not-verified`; 502 `auth-unavailable` si darmoz-auth no responde. El `accessToken` dura 12 minutos.")
    public AuthResponse login(@Valid @RequestBody AuthRequests.Login payload) {
        return authService.login(payload.email(), payload.password());
    }

    @PostMapping("/refresh")
    @Operation(summary = "Renovar tokens", description = "Rota el refresh token. 401 `invalid-session` si ya no es válido; 403 `not-admin` si le sacaron el rol.")
    public AuthResponse refresh(@Valid @RequestBody AuthRequests.Refresh payload) {
        return authService.refresh(payload.refreshToken());
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Cerrar sesión", description = "Revoca el refresh token; si se manda el Bearer, también bloquea el access token.")
    public void logout(@Valid @RequestBody AuthRequests.Logout payload,
                       @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        String accessToken = authorization != null && authorization.startsWith(BEARER_PREFIX)
                ? authorization.substring(BEARER_PREFIX.length()) : null;
        authService.logout(payload.refreshToken(), accessToken);
    }
}
