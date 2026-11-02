package com.darmoz.tramiteaadmin.service;

import com.darmoz.tramiteaadmin.dto.response.StatsResponse;
import com.darmoz.tramiteaadmin.repository.CategoryRepository;
import com.darmoz.tramiteaadmin.repository.InstitutionRepository;
import com.darmoz.tramiteaadmin.repository.ProcedureRepository;
import com.darmoz.tramiteaadmin.repository.ProcedureSuggestionRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StatsService {

    private final ProcedureRepository procedureRepository;
    private final CategoryRepository categoryRepository;
    private final InstitutionRepository institutionRepository;
    private final ProcedureSuggestionRepository suggestionRepository;
    private final JdbcTemplate jdbcTemplate;

    public StatsService(ProcedureRepository procedureRepository, CategoryRepository categoryRepository,
                        InstitutionRepository institutionRepository,
                        ProcedureSuggestionRepository suggestionRepository, JdbcTemplate jdbcTemplate) {
        this.procedureRepository = procedureRepository;
        this.categoryRepository = categoryRepository;
        this.institutionRepository = institutionRepository;
        this.suggestionRepository = suggestionRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(readOnly = true)
    public StatsResponse get() {
        return new StatsResponse(procedureRepository.count(), categoryRepository.count(),
                institutionRepository.count(), suggestionRepository.count(), countRegisteredUsers());
    }

    /**
     * Ciudadanos con perfil en Tramitea (tr_users; en testing se crea en el
     * primer request con token de darmoz-auth). Sin entidad propia: el admin
     * no edita usuarios.
     */
    private long countRegisteredUsers() {
        Long count = jdbcTemplate.queryForObject("select count(*) from tr_users", Long.class);
        return count == null ? 0 : count;
    }
}
