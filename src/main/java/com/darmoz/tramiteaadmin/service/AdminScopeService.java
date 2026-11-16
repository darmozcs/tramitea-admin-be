package com.darmoz.tramiteaadmin.service;

import com.darmoz.tramiteaadmin.entity.Location;
import com.darmoz.tramiteaadmin.exception.ApiException;
import com.darmoz.tramiteaadmin.repository.LocationRepository;
import com.darmoz.tramiteaadmin.security.AuthenticatedAdmin;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * País que administra el admin del request: el de su ubicación en tr_users
 * (la misma cuenta que usa en la web, vinculada por id_auth_user). Si su
 * ubicación es una provincia, administra igual todo el país.
 */
@Service
public class AdminScopeService {

    private final JdbcTemplate jdbcTemplate;
    private final LocationRepository locationRepository;

    public AdminScopeService(JdbcTemplate jdbcTemplate, LocationRepository locationRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.locationRepository = locationRepository;
    }

    /** País del admin y todas sus ubicaciones (el país y sus provincias, habilitadas o no). */
    public record Scope(UUID adminAuthUserId, UUID countryId, Set<UUID> locationIds) {

        public boolean contains(UUID locationId) {
            return locationId != null && locationIds.contains(locationId);
        }

        /** 403 si la ubicación no es de su país. */
        public void require(UUID locationId) {
            if (!contains(locationId)) {
                throw ApiException.forbidden(ApiException.LOCATION_NOT_ALLOWED,
                        "La ubicación no pertenece al país que administrás");
            }
        }
    }

    /** 403 `no-location` si el admin todavía no tiene ubicación en Tramitea. */
    @Transactional(readOnly = true)
    public Scope current() {
        AuthenticatedAdmin admin = currentAdmin();
        List<UUID> locations = jdbcTemplate.queryForList(
                "select location_id from tr_users where id_auth_user = ? and location_id is not null",
                UUID.class, admin.authUserId());
        Location home = locations.isEmpty() ? null : locationRepository.findById(locations.get(0)).orElse(null);
        if (home == null) {
            throw ApiException.forbidden(ApiException.NO_LOCATION,
                    "Tu usuario no tiene ubicación en Tramitea: asignale una provincia o un país en tr_users");
        }

        UUID countryId = home.getCountryId();
        Set<UUID> ids = locationRepository.findCountryAndProvinces(countryId).stream()
                .map(Location::getId)
                .collect(Collectors.toUnmodifiableSet());
        return new Scope(admin.authUserId(), countryId, ids);
    }

    private static AuthenticatedAdmin currentAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedAdmin admin)) {
            throw new IllegalStateException("Request sin admin autenticado");
        }
        return admin;
    }
}
