package com.darmoz.tramiteaadmin.service;

import com.darmoz.tramiteaadmin.dto.response.StatsResponse;
import com.darmoz.tramiteaadmin.entity.ProcedureSuggestion;
import com.darmoz.tramiteaadmin.entity.SuggestionStatus;
import com.darmoz.tramiteaadmin.repository.CategoryRepository;
import com.darmoz.tramiteaadmin.repository.InstitutionRepository;
import com.darmoz.tramiteaadmin.repository.ProcedureRepository;
import com.darmoz.tramiteaadmin.repository.ProcedureSuggestionRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

/** Totales del país del admin. Las categorías son globales. */
@Service
public class StatsService {

    private final ProcedureRepository procedureRepository;
    private final CategoryRepository categoryRepository;
    private final InstitutionRepository institutionRepository;
    private final ProcedureSuggestionRepository suggestionRepository;
    private final AdminScopeService adminScopeService;
    private final JdbcTemplate jdbcTemplate;

    public StatsService(ProcedureRepository procedureRepository, CategoryRepository categoryRepository,
                        InstitutionRepository institutionRepository,
                        ProcedureSuggestionRepository suggestionRepository, AdminScopeService adminScopeService,
                        JdbcTemplate jdbcTemplate) {
        this.procedureRepository = procedureRepository;
        this.categoryRepository = categoryRepository;
        this.institutionRepository = institutionRepository;
        this.suggestionRepository = suggestionRepository;
        this.adminScopeService = adminScopeService;
        this.jdbcTemplate = jdbcTemplate;
    }

    /** `suggestions` cuenta solo las pendientes de revisar. */
    @Transactional(readOnly = true)
    public StatsResponse get() {
        AdminScopeService.Scope scope = adminScopeService.current();
        Set<UUID> locations = scope.locationIds();
        Specification<ProcedureSuggestion> pending = StatsService.<ProcedureSuggestion>inLocations(locations)
                .and((root, query, cb) -> cb.equal(root.get("status"), SuggestionStatus.PENDING));
        return new StatsResponse(
                procedureRepository.count(inLocations(locations)),
                categoryRepository.count(),
                institutionRepository.count(inLocations(locations)),
                suggestionRepository.count(pending),
                countRegisteredUsers(scope.countryId()));
    }

    /**
     * Ciudadanos con perfil en Tramitea (tr_users) y ubicación en el país.
     * Sin entidad propia: el admin no edita usuarios.
     */
    private long countRegisteredUsers(UUID countryId) {
        Long count = jdbcTemplate.queryForObject("""
                select count(*) from tr_users
                where location_id in (select id from tr_locations where id = ? or parent_id = ?)""",
                Long.class, countryId, countryId);
        return count == null ? 0 : count;
    }

    private static <T> Specification<T> inLocations(Set<UUID> locationIds) {
        return (root, query, cb) -> root.get("locationId").in(locationIds);
    }
}
