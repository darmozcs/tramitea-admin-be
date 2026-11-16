package com.darmoz.tramiteaadmin.controller;

import com.darmoz.tramiteaadmin.dto.request.CatalogRequests;
import com.darmoz.tramiteaadmin.dto.response.PaginatedResponse;
import com.darmoz.tramiteaadmin.dto.response.SuggestionResponse;
import com.darmoz.tramiteaadmin.entity.SuggestionStatus;
import com.darmoz.tramiteaadmin.service.SuggestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Sugeridos", description = "Trámites sugeridos por los ciudadanos del país que administra el admin.")
@RestController
@RequestMapping("/suggestions")
public class SuggestionController {

    private final SuggestionService suggestionService;

    public SuggestionController(SuggestionService suggestionService) {
        this.suggestionService = suggestionService;
    }

    @GetMapping
    @Operation(summary = "Trámites sugeridos", description = "Paginado, los más nuevos primero. `status` opcional (`PENDING`, `ACCEPTED`, `REJECTED`). `page` desde 1, `pageSize` de 1 a 100.")
    public PaginatedResponse<SuggestionResponse> list(@RequestParam(required = false) SuggestionStatus status,
                                                      @RequestParam(defaultValue = "1") int page,
                                                      @RequestParam(defaultValue = "12") int pageSize) {
        return suggestionService.list(status, page, pageSize);
    }

    @PostMapping("/{id}/accept")
    @Operation(summary = "Aceptar una sugerencia", description = "Crea el trámite con el body (igual que `POST /procedures`) y la marca `ACCEPTED` con su `procedureId`. 409 `already-reviewed` si no está pendiente.")
    public SuggestionResponse accept(@PathVariable UUID id, @Valid @RequestBody CatalogRequests.SaveProcedure payload) {
        return suggestionService.accept(id, payload);
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "Rechazar una sugerencia", description = "`reason` opcional, hasta 1000. 409 `already-reviewed` si no está pendiente.")
    public SuggestionResponse reject(@PathVariable UUID id,
                                     @Valid @RequestBody(required = false) CatalogRequests.RejectSuggestion payload) {
        return suggestionService.reject(id, payload);
    }
}
