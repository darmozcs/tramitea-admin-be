package com.darmoz.tramiteaadmin.security;

import java.util.Set;
import java.util.UUID;

/** Usuario de darmoz-auth (claim sub) que hizo el request. */
public record AuthenticatedAdmin(UUID authUserId, String email, Set<String> roles) {
}
