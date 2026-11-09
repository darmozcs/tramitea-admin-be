package com.darmoz.tramiteaadmin.controller;

import com.darmoz.tramiteaadmin.dto.response.StatsResponse;
import com.darmoz.tramiteaadmin.service.StatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Dashboard", description = "Métricas del país que administra el admin.")
@RestController
public class DashboardController {

    private final StatsService statsService;

    public DashboardController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/stats")
    @Operation(summary = "Totales del dashboard", description = "Trámites, instituciones, sugerencias pendientes y usuarios registrados del país del admin. Las categorías son globales.")
    public StatsResponse stats() {
        return statsService.get();
    }
}
