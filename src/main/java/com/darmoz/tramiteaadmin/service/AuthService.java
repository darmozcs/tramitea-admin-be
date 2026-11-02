package com.darmoz.tramiteaadmin.service;

import com.darmoz.tramiteaadmin.config.DarmozAuthProperties;
import com.darmoz.tramiteaadmin.dto.response.AuthResponse;
import com.darmoz.tramiteaadmin.exception.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * Login del admin: las credenciales las valida darmoz-auth y acá solo se deja
 * pasar a quien tiene el rol admin. Si no lo tiene se revoca la sesión que
 * darmoz-auth acaba de abrir, para no dejar tokens sueltos.
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final DarmozAuthClient darmozAuthClient;
    private final String adminRole;

    public AuthService(DarmozAuthClient darmozAuthClient, DarmozAuthProperties properties) {
        this.darmozAuthClient = darmozAuthClient;
        this.adminRole = properties.getAdminRole();
    }

    public AuthResponse login(String email, String password) {
        return requireAdmin(darmozAuthClient.login(email, password));
    }

    /** Los roles se recalculan en cada refresh: si le sacaron el rol, se corta acá. */
    public AuthResponse refresh(String refreshToken) {
        return requireAdmin(darmozAuthClient.refresh(refreshToken));
    }

    public void logout(String refreshToken, String accessToken) {
        darmozAuthClient.logout(refreshToken, accessToken);
    }

    private AuthResponse requireAdmin(AuthResponse auth) {
        if (auth.roles() != null && auth.roles().contains(adminRole)) {
            return auth;
        }
        try {
            darmozAuthClient.logout(auth.refreshToken(), auth.accessToken());
        } catch (ApiException e) {
            log.warn("No se pudo revocar la sesión de un usuario sin rol admin: {}", e.getMessage());
        }
        throw new ApiException(HttpStatus.FORBIDDEN, ApiException.NOT_ADMIN,
                "El usuario no tiene permisos de administrador");
    }
}
