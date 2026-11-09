package com.darmoz.tramiteaadmin.service;

import com.darmoz.tramiteaadmin.dto.request.CatalogRequests;
import com.darmoz.tramiteaadmin.dto.response.PaginatedResponse;
import com.darmoz.tramiteaadmin.dto.response.SuggestionResponse;
import com.darmoz.tramiteaadmin.entity.ProcedureSuggestion;
import com.darmoz.tramiteaadmin.entity.SuggestionStatus;
import com.darmoz.tramiteaadmin.exception.ApiException;
import com.darmoz.tramiteaadmin.exception.NotFoundException;
import com.darmoz.tramiteaadmin.repository.ProcedureSuggestionRepository;
import com.darmoz.tramiteaadmin.util.TextNormalizer;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/** Sugerencias del país que administra el admin; las de otros países no existen para él (404). */
@Service
public class SuggestionService {

    private final ProcedureSuggestionRepository suggestionRepository;
    private final ProcedureService procedureService;
    private final AdminScopeService adminScopeService;

    public SuggestionService(ProcedureSuggestionRepository suggestionRepository, ProcedureService procedureService,
                             AdminScopeService adminScopeService) {
        this.suggestionRepository = suggestionRepository;
        this.procedureService = procedureService;
        this.adminScopeService = adminScopeService;
    }

    /** Las más nuevas primero. {@code status} null trae todas. */
    @Transactional(readOnly = true)
    public PaginatedResponse<SuggestionResponse> list(SuggestionStatus status, int page, int pageSize) {
        AdminScopeService.Scope scope = adminScopeService.current();
        Specification<ProcedureSuggestion> spec =
                (root, query, cb) -> root.get("locationId").in(scope.locationIds());
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }

        long total = suggestionRepository.count(spec);
        Paging.Window window = Paging.window(total, page, pageSize,
                Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by("id")));
        List<SuggestionResponse> items = suggestionRepository.findAll(spec, window.pageRequest())
                .map(SuggestionResponse::from)
                .getContent();
        return window.response(total, items);
    }

    /**
     * Crea el trámite con {@code payload} (mismas reglas que POST /procedures:
     * su ubicación puede ser cualquiera del país, no necesariamente la de la
     * sugerencia) y la marca aceptada.
     */
    @Transactional
    public SuggestionResponse accept(UUID id, CatalogRequests.SaveProcedure payload) {
        ProcedureSuggestion suggestion = findPending(id);
        UUID procedureId = procedureService.create(payload).id();
        suggestion.accept(adminScopeService.current().adminAuthUserId(), procedureId);
        return SuggestionResponse.from(suggestionRepository.save(suggestion));
    }

    @Transactional
    public SuggestionResponse reject(UUID id, CatalogRequests.RejectSuggestion payload) {
        ProcedureSuggestion suggestion = findPending(id);
        String reason = payload == null ? null : TextNormalizer.trimToNull(payload.reason());
        suggestion.reject(adminScopeService.current().adminAuthUserId(), reason);
        return SuggestionResponse.from(suggestionRepository.save(suggestion));
    }

    private ProcedureSuggestion findPending(UUID id) {
        AdminScopeService.Scope scope = adminScopeService.current();
        ProcedureSuggestion suggestion = suggestionRepository.findById(id)
                .filter(s -> scope.contains(s.getLocationId()))
                .orElseThrow(() -> new NotFoundException("Sugerencia no encontrada"));
        if (suggestion.getStatus() != SuggestionStatus.PENDING) {
            throw ApiException.conflict(ApiException.ALREADY_REVIEWED, "La sugerencia ya fue revisada");
        }
        return suggestion;
    }
}
