package com.darmoz.tramiteaadmin.dto.request;

import jakarta.validation.constraints.NotBlank;

/** Mismos bodies que darmoz-auth, que es quien los procesa. */
public final class AuthRequests {

    private AuthRequests() {
    }

    public record Login(@NotBlank String email, @NotBlank String password) {
    }

    public record Refresh(@NotBlank String refreshToken) {
    }

    public record Logout(@NotBlank String refreshToken) {
    }
}
