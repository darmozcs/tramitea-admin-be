package com.darmoz.tramiteaadmin.controller;

import com.darmoz.tramiteaadmin.dto.request.CatalogRequests;
import com.darmoz.tramiteaadmin.dto.response.CategoryResponse;
import com.darmoz.tramiteaadmin.dto.response.PaginatedResponse;
import com.darmoz.tramiteaadmin.service.CategoryService;
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

@Tag(name = "Categorías")
@RestController
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    @Operation(summary = "Listar categorías", description = "Paginado. Incluye las inactivas. `search` busca en nombre y código; `isActive` filtra por estado. Ordenadas por `displayOrder`. `page` desde 1, `pageSize` de 1 a 100. Para combos, `GET /categories/catalog`.")
    public PaginatedResponse<CategoryResponse> list(@RequestParam(required = false) String search,
                                                    @RequestParam(required = false) Boolean isActive,
                                                    @RequestParam(defaultValue = "1") int page,
                                                    @RequestParam(defaultValue = "12") int pageSize) {
        return categoryService.list(search, isActive, page, pageSize);
    }

    @GetMapping("/catalog")
    @Operation(summary = "Catálogo de categorías", description = "Todas, sin paginar, para llenar combos. Mismos filtros y orden que el listado; para el formulario de trámite conviene `?isActive=true`.")
    public List<CategoryResponse> catalog(@RequestParam(required = false) String search,
                                          @RequestParam(required = false) Boolean isActive) {
        return categoryService.catalog(search, isActive);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalle de una categoría")
    public CategoryResponse get(@PathVariable UUID id) {
        return categoryService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear categoría", description = "409 `duplicated` si el código ya existe.")
    public CategoryResponse create(@Valid @RequestBody CatalogRequests.SaveCategory payload) {
        return categoryService.create(payload);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Editar categoría", description = "Reemplaza todos los campos. 409 `duplicated` si el código ya existe.")
    public CategoryResponse update(@PathVariable UUID id, @Valid @RequestBody CatalogRequests.SaveCategory payload) {
        return categoryService.update(id, payload);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Borrar categoría", description = "409 `in-use` si tiene trámites: en ese caso conviene desactivarla.")
    public void delete(@PathVariable UUID id) {
        categoryService.delete(id);
    }
}
