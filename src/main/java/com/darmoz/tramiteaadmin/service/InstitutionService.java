package com.darmoz.tramiteaadmin.service;

import com.darmoz.tramiteaadmin.dto.request.CatalogRequests;
import com.darmoz.tramiteaadmin.dto.response.InstitutionResponse;
import com.darmoz.tramiteaadmin.dto.response.PaginatedResponse;
import com.darmoz.tramiteaadmin.entity.Institution;
import com.darmoz.tramiteaadmin.exception.ApiException;
import com.darmoz.tramiteaadmin.exception.NotFoundException;
import com.darmoz.tramiteaadmin.repository.InstitutionRepository;
import com.darmoz.tramiteaadmin.repository.ProcedureRepository;
import com.darmoz.tramiteaadmin.repository.ProcedureSuggestionRepository;
import com.darmoz.tramiteaadmin.util.TextNormalizer;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class InstitutionService {

    private final InstitutionRepository institutionRepository;
    private final ProcedureRepository procedureRepository;
    private final ProcedureSuggestionRepository suggestionRepository;
    private final AdminScopeService adminScopeService;

    public InstitutionService(InstitutionRepository institutionRepository, ProcedureRepository procedureRepository,
                              ProcedureSuggestionRepository suggestionRepository,
                              AdminScopeService adminScopeService) {
        this.institutionRepository = institutionRepository;
        this.procedureRepository = procedureRepository;
        this.suggestionRepository = suggestionRepository;
        this.adminScopeService = adminScopeService;
    }

    /** Orden del listado y del catálogo; `id` al final para que la paginación sea estable. */
    private static final Sort ORDER = Sort.by("acronym", "id");

    /** Paginado, para la pantalla de instituciones. Solo las del país que administra. */
    @Transactional(readOnly = true)
    public PaginatedResponse<InstitutionResponse> list(String search, int page, int pageSize) {
        Specification<Institution> spec = spec(search);
        long total = institutionRepository.count(spec);
        Paging.Window window = Paging.window(total, page, pageSize, ORDER);
        List<InstitutionResponse> items = institutionRepository.findAll(spec, window.pageRequest())
                .map(InstitutionResponse::from)
                .getContent();
        return window.response(total, items);
    }

    /** Todas las del país que administra, sin paginar, para los combos. */
    @Transactional(readOnly = true)
    public List<InstitutionResponse> catalog(String search) {
        return institutionRepository.findAll(spec(search), ORDER).stream()
                .map(InstitutionResponse::from)
                .toList();
    }

    private Specification<Institution> spec(String search) {
        AdminScopeService.Scope scope = adminScopeService.current();
        String term = TextNormalizer.trimToNull(search);
        Specification<Institution> inScope = (root, query, cb) -> root.get("locationId").in(scope.locationIds());
        return term == null ? inScope : inScope.and((root, query, cb) -> {
            String pattern = "%" + Specs.escapeLike(term.toLowerCase()) + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("name")), pattern, '\\'),
                    cb.like(cb.lower(root.get("acronym")), pattern, '\\'));
        });
    }

    @Transactional(readOnly = true)
    public InstitutionResponse get(UUID id) {
        return InstitutionResponse.from(find(id));
    }

    @Transactional
    public InstitutionResponse create(CatalogRequests.SaveInstitution payload) {
        return save(new Institution(UUID.randomUUID()), payload);
    }

    @Transactional
    public InstitutionResponse update(UUID id, CatalogRequests.SaveInstitution payload) {
        return save(find(id), payload);
    }

    /** Las sugerencias también apuntan a la institución (FK sin cascade). */
    @Transactional
    public void delete(UUID id) {
        Institution institution = find(id);
        if (procedureRepository.existsByInstitutionId(id) || suggestionRepository.existsByInstitutionId(id)) {
            throw ApiException.conflict(ApiException.IN_USE, "La institución tiene trámites o sugerencias asociadas");
        }
        institutionRepository.delete(institution);
    }

    private InstitutionResponse save(Institution institution, CatalogRequests.SaveInstitution payload) {
        adminScopeService.current().require(payload.locationId());
        institution.update(payload.acronym().trim(), payload.name().trim(),
                TextNormalizer.trimToNull(payload.parentEntity()), payload.officialWebsite().trim(),
                TextNormalizer.trimToNull(payload.contactPhone()), TextNormalizer.trimToNull(payload.contactEmail()),
                TextNormalizer.trimToNull(payload.logoUrl()), payload.locationId());
        return InstitutionResponse.from(institutionRepository.save(institution));
    }

    /** Las de otros países no existen para este admin: 404. */
    private Institution find(UUID id) {
        AdminScopeService.Scope scope = adminScopeService.current();
        return institutionRepository.findById(id)
                .filter(i -> scope.contains(i.getLocationId()))
                .orElseThrow(() -> new NotFoundException("Institución no encontrada"));
    }
}
