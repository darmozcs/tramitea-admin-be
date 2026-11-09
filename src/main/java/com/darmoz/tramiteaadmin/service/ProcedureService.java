package com.darmoz.tramiteaadmin.service;

import com.darmoz.tramiteaadmin.dto.request.CatalogRequests;
import com.darmoz.tramiteaadmin.dto.response.PaginatedResponse;
import com.darmoz.tramiteaadmin.dto.response.ProcedureResponse;
import com.darmoz.tramiteaadmin.entity.Procedure;
import com.darmoz.tramiteaadmin.entity.ProcedureFaq;
import com.darmoz.tramiteaadmin.entity.ProcedureStep;
import com.darmoz.tramiteaadmin.entity.ProcedureStepComment;
import com.darmoz.tramiteaadmin.entity.Requirement;
import com.darmoz.tramiteaadmin.exception.ApiException;
import com.darmoz.tramiteaadmin.exception.NotFoundException;
import com.darmoz.tramiteaadmin.repository.CategoryRepository;
import com.darmoz.tramiteaadmin.repository.InstitutionRepository;
import com.darmoz.tramiteaadmin.repository.ProcedureRepository;
import com.darmoz.tramiteaadmin.util.TextNormalizer;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

@Service
public class ProcedureService {

    private final ProcedureRepository procedureRepository;
    private final CategoryRepository categoryRepository;
    private final InstitutionRepository institutionRepository;
    private final AdminScopeService adminScopeService;

    public ProcedureService(ProcedureRepository procedureRepository, CategoryRepository categoryRepository,
                            InstitutionRepository institutionRepository, AdminScopeService adminScopeService) {
        this.procedureRepository = procedureRepository;
        this.categoryRepository = categoryRepository;
        this.institutionRepository = institutionRepository;
        this.adminScopeService = adminScopeService;
    }

    /**
     * Mismos filtros que el listado de tramitea-be, que es lo que hoy usa el
     * admin, solo con trámites del país que administra (habilitados o no).
     */
    @Transactional(readOnly = true)
    public PaginatedResponse<ProcedureResponse> list(String searchTerm, List<UUID> categoryIds,
                                                     List<UUID> institutionIds, boolean isFreeOnly,
                                                     int page, int pageSize) {
        AdminScopeService.Scope scope = adminScopeService.current();
        Specification<Procedure> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(root.get("locationId").in(scope.locationIds()));
            String term = TextNormalizer.normalize(searchTerm).trim();
            if (!term.isEmpty()) {
                predicates.add(cb.like(root.get("searchText"), "%" + Specs.escapeLike(term) + "%", '\\'));
            }
            if (isPresent(categoryIds)) {
                predicates.add(root.get("categoryId").in(categoryIds));
            }
            if (isPresent(institutionIds)) {
                predicates.add(root.get("institutionId").in(institutionIds));
            }
            if (isFreeOnly) {
                predicates.add(cb.isTrue(root.get("free")));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };

        long total = procedureRepository.count(spec);
        Paging.Window window = Paging.window(total, page, pageSize,
                Sort.by("displayOrder", "name"));
        List<ProcedureResponse> items = procedureRepository.findAll(spec, window.pageRequest())
                .map(ProcedureResponse::from)
                .getContent();
        return window.response(total, items);
    }

    @Transactional(readOnly = true)
    public ProcedureResponse get(UUID id) {
        return ProcedureResponse.from(find(id));
    }

    @Transactional
    public ProcedureResponse create(CatalogRequests.SaveProcedure payload) {
        return save(new Procedure(UUID.randomUUID()), payload);
    }

    @Transactional
    public ProcedureResponse update(UUID id, CatalogRequests.SaveProcedure payload) {
        return save(find(id), payload);
    }

    /**
     * La base borra en cascada requisitos, pasos, FAQs, comentarios, votos y
     * las tareas guardadas de los ciudadanos.
     */
    @Transactional
    public void delete(UUID id) {
        procedureRepository.delete(find(id));
    }

    /** La ubicación tiene que ser del país del admin, y la institución también. */
    private ProcedureResponse save(Procedure procedure, CatalogRequests.SaveProcedure payload) {
        AdminScopeService.Scope scope = adminScopeService.current();
        scope.require(payload.locationId());
        if (!categoryRepository.existsById(payload.categoryId())) {
            throw ApiException.badRequest("categoryId no existe");
        }
        boolean institutionInScope = institutionRepository.findById(payload.institutionId())
                .map(i -> scope.contains(i.getLocationId()))
                .orElse(false);
        if (!institutionInScope) {
            throw ApiException.badRequest("institutionId no existe o es de otro país");
        }
        // El front muestra el costo siempre que no sea gratis.
        if (!payload.isFree() && payload.cost() == null) {
            throw ApiException.badRequest("cost es obligatorio si el trámite no es gratuito");
        }

        String name = payload.name().trim();
        String description = payload.description().trim();
        String slugEs = slugOrDefault(payload.slug() == null ? null : payload.slug().es(), name);
        String slugEn = slugOrDefault(payload.slug() == null ? null : payload.slug().en(), slugEs);
        if (procedureRepository.existsSlugOutside(procedure.getId(), slugEs, slugEn)) {
            throw ApiException.conflict(ApiException.DUPLICATED, "Ya hay un trámite con el slug " + slugEs
                    + (slugEs.equals(slugEn) ? "" : " o " + slugEn));
        }

        CatalogRequests.Cost cost = payload.isFree() ? null : payload.cost();
        CatalogRequests.Validity validity = payload.validity();
        procedure.update(new Procedure.Details(
                name, payload.applicationMethod(), payload.authenticationLevel(), payload.group(), description,
                payload.imageUrl().trim(), payload.categoryId(), payload.institutionId(), payload.locationId(),
                payload.appointmentRequired(), payload.isFree(),
                cost == null ? null : cost.amount(),
                cost == null ? null : cost.currency(),
                cost == null ? null : TextNormalizer.trimToNull(cost.comments()),
                validity == null ? null : validity.value().trim(),
                validity == null ? null : TextNormalizer.trimToNull(validity.unit()),
                validity == null ? null : TextNormalizer.trimToNull(validity.comments()),
                TextNormalizer.trimToNull(payload.estimatedDeliveryTime()),
                slugEs, slugEn,
                payload.displayOrder() == null ? 0 : payload.displayOrder()),
                // Misma regla que el seed de tramitea-be: nombre + descripción, normalizados.
                TextNormalizer.normalize(name + " " + description));

        procedure.replaceChildren(requirements(procedure, payload.requirements()),
                steps(procedure, payload.steps()), faqs(procedure, payload.faqs()));
        return ProcedureResponse.from(procedureRepository.saveAndFlush(procedure));
    }

    private static List<Requirement> requirements(Procedure procedure, List<CatalogRequests.Requirement> items) {
        List<CatalogRequests.Requirement> list = orEmpty(items);
        return IntStream.range(0, list.size())
                .mapToObj(i -> new Requirement(procedure, list.get(i).title().trim(),
                        TextNormalizer.trimToNull(list.get(i).description()), list.get(i).isRequired(),
                        list.get(i).isDigitallyVerifiable(), i + 1))
                .toList();
    }

    private static List<ProcedureStep> steps(Procedure procedure, List<CatalogRequests.Step> items) {
        List<CatalogRequests.Step> list = orEmpty(items);
        return IntStream.range(0, list.size())
                .mapToObj(i -> {
                    CatalogRequests.Step step = list.get(i);
                    List<ProcedureStepComment> comments = orEmpty(step.comments()).stream()
                            .map(c -> new ProcedureStepComment(c.level(), c.content().trim()))
                            .toList();
                    return new ProcedureStep(procedure, i + 1, step.title().trim(), step.instructions().trim(),
                            TextNormalizer.trimToNull(step.channelUrl()),
                            TextNormalizer.trimToNull(step.estimatedTime()), comments);
                })
                .toList();
    }

    private static List<ProcedureFaq> faqs(Procedure procedure, List<CatalogRequests.Faq> items) {
        List<CatalogRequests.Faq> list = orEmpty(items);
        return IntStream.range(0, list.size())
                .mapToObj(i -> new ProcedureFaq(procedure, list.get(i).question().trim(),
                        list.get(i).answer().trim(), i + 1))
                .toList();
    }

    private static String slugOrDefault(String slug, String fallback) {
        String value = TextNormalizer.slugify(slug == null || slug.isBlank() ? fallback : slug);
        if (value.isEmpty()) {
            throw ApiException.badRequest("No se pudo armar un slug válido a partir de \"" + fallback + "\"");
        }
        return value;
    }

    /** Los trámites de otros países no existen para este admin: 404. */
    private Procedure find(UUID id) {
        AdminScopeService.Scope scope = adminScopeService.current();
        return procedureRepository.findById(id)
                .filter(p -> scope.contains(p.getLocationId()))
                .orElseThrow(() -> new NotFoundException("Trámite no encontrado"));
    }

    private static boolean isPresent(Collection<?> values) {
        return values != null && !values.isEmpty();
    }

    private static <T> List<T> orEmpty(List<T> values) {
        return values == null ? List.of() : values;
    }
}
