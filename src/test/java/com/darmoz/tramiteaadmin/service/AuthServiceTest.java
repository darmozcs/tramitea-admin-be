package com.darmoz.tramiteaadmin.service;

import com.darmoz.tramiteaadmin.config.DarmozAuthProperties;
import com.darmoz.tramiteaadmin.dto.response.AuthResponse;
import com.darmoz.tramiteaadmin.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private DarmozAuthClient darmozAuthClient;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        DarmozAuthProperties properties = new DarmozAuthProperties();
        properties.setAdminRole("ADMIN");
        authService = new AuthService(darmozAuthClient, properties);
    }

    @Test
    void adminGetsTheTokens() {
        AuthResponse admin = auth(Set.of("USER", "ADMIN"));
        when(darmozAuthClient.login("admin@example.com", "secreta")).thenReturn(admin);

        assertThat(authService.login("admin@example.com", "secreta")).isEqualTo(admin);
        verify(darmozAuthClient, never()).logout(any(), any());
    }

    @Test
    void userWithoutRoleIsRejectedAndItsSessionRevoked() {
        AuthResponse citizen = auth(Set.of("USER"));
        when(darmozAuthClient.login("ana@example.com", "secreta")).thenReturn(citizen);

        assertThatThrownBy(() -> authService.login("ana@example.com", "secreta"))
                .isInstanceOfSatisfying(ApiException.class, e -> {
                    assertThat(e.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
                    assertThat(e.getCode()).isEqualTo(ApiException.NOT_ADMIN);
                });
        verify(darmozAuthClient).logout("refresh", "access");
    }

    @Test
    void stillRejectsWhenRevokingFails() {
        when(darmozAuthClient.refresh("refresh")).thenReturn(auth(Set.of()));
        doThrow(new ApiException(HttpStatus.BAD_GATEWAY, ApiException.AUTH_UNAVAILABLE, "caído"))
                .when(darmozAuthClient).logout(any(), any());

        assertThatThrownBy(() -> authService.refresh("refresh"))
                .isInstanceOfSatisfying(ApiException.class,
                        e -> assertThat(e.getCode()).isEqualTo(ApiException.NOT_ADMIN));
    }

    private static AuthResponse auth(Set<String> roles) {
        return new AuthResponse(UUID.randomUUID(), "x@example.com", roles, "access", "refresh", "Bearer", 720);
    }
}
