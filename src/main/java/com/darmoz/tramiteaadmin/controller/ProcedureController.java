package com.darmoz.tramiteaadmin.controller;

import com.darmoz.tramiteaadmin.dto.request.CatalogRequests;
import com.darmoz.tramiteaadmin.dto.response.PaginatedResponse;
import com.darmoz.tramiteaadmin.dto.response.ProcedureResponse;
import com.darmoz.tramiteaadmin.service.ProcedureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = "Trámites")
@RestController
@RequestMapping("/procedures")
public class ProcedureController {

    private final ProcedureService procedureService;

    public ProcedureController(ProcedureService procedureService) {
        this.procedureService = procedureService;
    }

    @GetMapping
    @Operation(summary = "Listar trámites", description = "Mismos parámetros que `GET /tramitea-api/procedures`: listas separadas por coma, `page` desde 1, `pageSize` de 1 a 100. Ordenados por `displayOrder`.")
    public PaginatedResponse<ProcedureResponse> list(
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) List<UUID> categoryIds,
            @RequestParam(required = false) List<UUID> institutionIds,
            @RequestParam(defaultValue = "false") boolean isFreeOnly,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int pageSize) {
        return procedureService.list(searchTerm, categoryIds, institutionIds, isFreeOnly, page, pageSize);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalle de un trámite", description = "Por id (no suma visitas, a diferencia del detalle público por slug).")
    public ProcedureResponse get(@PathVariable UUID id) {
        return procedureService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear trámite", description = "Con requisitos, pasos y FAQs; su orden es el del array. Sin `slug` se arma a partir del nombre. `cost` es obligatorio si `isFree` es false y se ignora si es true. 400 si `categoryId` o `institutionId` no existen; 409 `duplicated` si el slug ya está usado.")
    public ProcedureResponse create(@Valid @RequestBody CatalogRequests.SaveProcedure payload) {
        return procedureService.create(payload);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Editar trámite", description = "Reemplaza todo, incluidos requisitos, pasos y FAQs (los ids de esos hijos cambian). Mismas reglas que el alta.")
    public ProcedureResponse update(@PathVariable UUID id,
                                    @Valid @RequestBody CatalogRequests.SaveProcedure payload) {
        return procedureService.update(id, payload);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Borrar trámite", description = "Borra también sus comentarios, votos y las tareas guardadas de los ciudadanos.")
    public void delete(@PathVariable UUID id) {
        procedureService.delete(id);
    }
}
