package com.darmoz.tramiteaadmin.service;

import com.darmoz.tramiteaadmin.dto.request.CatalogRequests;
import com.darmoz.tramiteaadmin.dto.response.CategoryResponse;
import com.darmoz.tramiteaadmin.dto.response.PaginatedResponse;
import com.darmoz.tramiteaadmin.entity.Category;
import com.darmoz.tramiteaadmin.exception.ApiException;
import com.darmoz.tramiteaadmin.exception.NotFoundException;
import com.darmoz.tramiteaadmin.repository.CategoryRepository;
import com.darmoz.tramiteaadmin.repository.ProcedureRepository;
import com.darmoz.tramiteaadmin.util.TextNormalizer;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** A diferencia de tramitea-be, lista también las inactivas. */
@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProcedureRepository procedureRepository;

    public CategoryService(CategoryRepository categoryRepository, ProcedureRepository procedureRepository) {
        this.categoryRepository = categoryRepository;
        this.procedureRepository = procedureRepository;
    }

    /** Orden del listado y del catálogo; `id` al final para que la paginación sea estable. */
    private static final Sort ORDER = Sort.by("displayOrder", "name", "id");

    /** Paginado, para la pantalla de categorías. */
    @Transactional(readOnly = true)
    public PaginatedResponse<CategoryResponse> list(String search, Boolean isActive, int page, int pageSize) {
        Specification<Category> spec = spec(search, isActive);
        long total = categoryRepository.count(spec);
        Paging.Window window = Paging.window(total, page, pageSize, ORDER);
        List<CategoryResponse> items = categoryRepository.findAll(spec, window.pageRequest())
                .map(CategoryResponse::from)
                .getContent();
        return window.response(total, items);
    }

    /** Todas, sin paginar, para los combos. */
    @Transactional(readOnly = true)
    public List<CategoryResponse> catalog(String search, Boolean isActive) {
        return categoryRepository.findAll(spec(search, isActive), ORDER).stream()
                .map(CategoryResponse::from)
                .toList();
    }

    private static Specification<Category> spec(String search, Boolean isActive) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            String term = TextNormalizer.trimToNull(search);
            if (term != null) {
                String pattern = "%" + Specs.escapeLike(term.toLowerCase()) + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), pattern, '\\'),
                        cb.like(cb.lower(root.get("code")), pattern, '\\')));
            }
            if (isActive != null) {
                predicates.add(cb.equal(root.get("active"), isActive));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    @Transactional(readOnly = true)
    public CategoryResponse get(UUID id) {
        return CategoryResponse.from(find(id));
    }

    @Transactional
    public CategoryResponse create(CatalogRequests.SaveCategory payload) {
        return save(new Category(UUID.randomUUID()), payload);
    }

    @Transactional
    public CategoryResponse update(UUID id, CatalogRequests.SaveCategory payload) {
        return save(find(id), payload);
    }

    @Transactional
    public void delete(UUID id) {
        Category category = find(id);
        if (procedureRepository.existsByCategoryId(id)) {
            throw ApiException.conflict(ApiException.IN_USE,
                    "La categoría tiene trámites: reasignalos o desactivá la categoría");
        }
        categoryRepository.delete(category);
    }

    private CategoryResponse save(Category category, CatalogRequests.SaveCategory payload) {
        String code = payload.code().trim();
        if (categoryRepository.existsByCodeIgnoreCaseAndIdNot(code, category.getId())) {
            throw ApiException.conflict(ApiException.DUPLICATED, "Ya existe una categoría con el código " + code);
        }
        category.update(code, payload.name().trim(), TextNormalizer.trimToNull(payload.description()),
                payload.icon().trim(), payload.displayOrder(), payload.isActive());
        return CategoryResponse.from(categoryRepository.save(category));
    }

    private Category find(UUID id) {
        return categoryRepository.findById(id).orElseThrow(() -> new NotFoundException("Categoría no encontrada"));
    }
}
