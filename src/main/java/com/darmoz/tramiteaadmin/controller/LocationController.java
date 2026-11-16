package com.darmoz.tramiteaadmin.controller;

import com.darmoz.tramiteaadmin.dto.request.CatalogRequests;
import com.darmoz.tramiteaadmin.dto.response.LocationResponse;
import com.darmoz.tramiteaadmin.service.LocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = "Ubicaciones", description = "País que administra el admin y sus provincias.")
@RestController
@RequestMapping("/locations")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @GetMapping
    @Operation(summary = "Ubicaciones del admin", description = "Su país primero y después sus provincias por nombre, habilitadas o no.")
    public List<LocationResponse> list() {
        return locationService.list();
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Habilitar o deshabilitar", description = "Los trámites de una ubicación deshabilitada no se ven en la web; si es el país, ninguno del país. 404 si no es de su país.")
    public LocationResponse update(@PathVariable UUID id, @Valid @RequestBody CatalogRequests.UpdateLocation payload) {
        return locationService.changeActive(id, payload.isActive());
    }
}
