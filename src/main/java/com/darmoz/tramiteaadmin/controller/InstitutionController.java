package com.darmoz.tramiteaadmin.controller;

import com.darmoz.tramiteaadmin.dto.request.CatalogRequests;
import com.darmoz.tramiteaadmin.dto.response.InstitutionResponse;
import com.darmoz.tramiteaadmin.dto.response.PaginatedResponse;
import com.darmoz.tramiteaadmin.service.InstitutionService;
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

@Tag(name = "Instituciones")
@RestController
@RequestMapping("/institutions")
public class InstitutionController {

    private final InstitutionService institutionService;

    public InstitutionController(InstitutionService institutionService) {
        this.institutionService = institutionService;
    }

    @GetMapping
    @Operation(summary = "Listar instituciones", description = "Paginado. `search` busca en nombre y sigla. Ordenadas por sigla. Solo las del país del admin. `page` desde 1, `pageSize` de 1 a 100. Para combos, `GET /institutions/catalog`.")
    public PaginatedResponse<InstitutionResponse> list(@RequestParam(required = false) String search,
                                                       @RequestParam(defaultValue = "1") int page,
                                                       @RequestParam(defaultValue = "12") int pageSize) {
        return institutionService.list(search, page, pageSize);
    }

    @GetMapping("/catalog")
    @Operation(summary = "Catálogo de instituciones", description = "Todas las del país del admin, sin paginar, para llenar combos. Mismo filtro `search` y mismo orden que el listado.")
    public List<InstitutionResponse> catalog(@RequestParam(required = false) String search) {
        return institutionService.catalog(search);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalle de una institución", description = "404 si es de otro país.")
    public InstitutionResponse get(@PathVariable UUID id) {
        return institutionService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear institución", description = "`locationId` obligatorio: el país del admin o una de sus provincias; si es de otro país, 403 `location-not-allowed`.")
    public InstitutionResponse create(@Valid @RequestBody CatalogRequests.SaveInstitution payload) {
        return institutionService.create(payload);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Editar institución", description = "Reemplaza todos los campos. Mismas reglas de `locationId` que el alta; 404 si es de otro país.")
    public InstitutionResponse update(@PathVariable UUID id,
                                      @Valid @RequestBody CatalogRequests.SaveInstitution payload) {
        return institutionService.update(id, payload);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Borrar institución", description = "409 `in-use` si tiene trámites o sugerencias.")
    public void delete(@PathVariable UUID id) {
        institutionService.delete(id);
    }
}
