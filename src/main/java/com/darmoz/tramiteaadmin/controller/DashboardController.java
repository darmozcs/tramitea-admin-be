package com.darmoz.tramiteaadmin.controller;

import com.darmoz.tramiteaadmin.dto.response.PaginatedResponse;
import com.darmoz.tramiteaadmin.dto.response.StatsResponse;
import com.darmoz.tramiteaadmin.dto.response.SuggestionResponse;
import com.darmoz.tramiteaadmin.service.StatsService;
import com.darmoz.tramiteaadmin.service.SuggestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Dashboard", description = "Métricas y trámites sugeridos por los ciudadanos.")
@RestController
public class DashboardController {

    private final StatsService statsService;
    private final SuggestionService suggestionService;

    public DashboardController(StatsService statsService, SuggestionService suggestionService) {
        this.statsService = statsService;
        this.suggestionService = suggestionService;
    }

    @GetMapping("/stats")
    @Operation(summary = "Totales del dashboard", description = "Trámites, categorías, instituciones, sugerencias y usuarios registrados.")
    public StatsResponse stats() {
        return statsService.get();
    }

    @GetMapping("/suggestions")
    @Operation(summary = "Trámites sugeridos", description = "Paginado, los más nuevos primero. `page` desde 1, `pageSize` de 1 a 100.")
    public PaginatedResponse<SuggestionResponse> suggestions(@RequestParam(defaultValue = "1") int page,
                                                             @RequestParam(defaultValue = "12") int pageSize) {
        return suggestionService.list(page, pageSize);
    }
}
