package com.darmoz.tramiteaadmin.dto.response;

import com.darmoz.tramiteaadmin.entity.ProcedureSuggestion;
import com.darmoz.tramiteaadmin.entity.SuggestionStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * A diferencia de tramitea-be, el admin ve el detalle, el contacto y la
 * revisión. `reviewedBy` es el id de darmoz-auth del admin; `procedureId`, el
 * trámite creado al aceptarla.
 */
public record SuggestionResponse(UUID id, String title, UUID institutionId, UUID locationId, String details,
                                 String contactEmail, UUID userId, SuggestionStatus status, UUID reviewedBy,
                                 Instant reviewedAt, String rejectionReason, UUID procedureId, Instant createdAt) {

    public static SuggestionResponse from(ProcedureSuggestion s) {
        return new SuggestionResponse(s.getId(), s.getTitle(), s.getInstitutionId(), s.getLocationId(),
                s.getDetails(), s.getContactEmail(), s.getUserId(), s.getStatus(), s.getReviewedBy(),
                s.getReviewedAt(), s.getRejectionReason(), s.getProcedureId(), s.getCreatedAt());
    }
}
