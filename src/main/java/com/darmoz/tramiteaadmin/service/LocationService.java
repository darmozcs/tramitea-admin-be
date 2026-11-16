package com.darmoz.tramiteaadmin.service;

import com.darmoz.tramiteaadmin.dto.response.LocationResponse;
import com.darmoz.tramiteaadmin.entity.Location;
import com.darmoz.tramiteaadmin.exception.NotFoundException;
import com.darmoz.tramiteaadmin.repository.LocationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * País del admin y sus provincias. Deshabilitar una ubicación oculta sus
 * trámites en la web; deshabilitar el país oculta todos los del país.
 */
@Service
public class LocationService {

    private final LocationRepository locationRepository;
    private final AdminScopeService adminScopeService;

    public LocationService(LocationRepository locationRepository, AdminScopeService adminScopeService) {
        this.locationRepository = locationRepository;
        this.adminScopeService = adminScopeService;
    }

    @Transactional(readOnly = true)
    public List<LocationResponse> list() {
        return locationRepository.findCountryAndProvinces(adminScopeService.current().countryId()).stream()
                .map(LocationResponse::from)
                .toList();
    }

    @Transactional
    public LocationResponse changeActive(UUID id, boolean active) {
        AdminScopeService.Scope scope = adminScopeService.current();
        Location location = locationRepository.findById(id)
                .filter(l -> scope.contains(l.getId()))
                .orElseThrow(() -> new NotFoundException("Ubicación no encontrada"));
        location.changeActive(active);
        return LocationResponse.from(location);
    }
}
