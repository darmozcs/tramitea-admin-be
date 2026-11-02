package com.darmoz.tramiteaadmin.service;

import com.darmoz.tramiteaadmin.dto.request.CatalogRequests;
import com.darmoz.tramiteaadmin.dto.response.InstitutionResponse;
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

    public InstitutionService(InstitutionRepository institutionRepository, ProcedureRepository procedureRepository,
                              ProcedureSuggestionRepository suggestionRepository) {
        this.institutionRepository = institutionRepository;
        this.procedureRepository = procedureRepository;
        this.suggestionRepository = suggestionRepository;
    }

    @Transactional(readOnly = true)
    public List<InstitutionResponse> list(String search) {
        String term = TextNormalizer.trimToNull(search);
        Specification<Institution> spec = term == null ? Specification.where(null) : (root, query, cb) -> {
            String pattern = "%" + Specs.escapeLike(term.toLowerCase()) + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("name")), pattern, '\\'),
                    cb.like(cb.lower(root.get("acronym")), pattern, '\\'));
        };
        return institutionRepository.findAll(spec, Sort.by("acronym")).stream()
                .map(InstitutionResponse::from)
                .toList();
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
        institution.update(payload.acronym().trim(), payload.name().trim(),
                TextNormalizer.trimToNull(payload.parentEntity()), payload.officialWebsite().trim(),
                TextNormalizer.trimToNull(payload.contactPhone()), TextNormalizer.trimToNull(payload.contactEmail()),
                TextNormalizer.trimToNull(payload.logoUrl()));
        return InstitutionResponse.from(institutionRepository.save(institution));
    }

    private Institution find(UUID id) {
        return institutionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Institución no encontrada"));
    }
}
