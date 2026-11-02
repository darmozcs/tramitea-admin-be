package com.darmoz.tramiteaadmin.dto.response;

import com.darmoz.tramiteaadmin.entity.ProcedureSuggestion;

import java.time.Instant;
import java.util.UUID;

/** A diferencia de tramitea-be, el admin ve el detalle y el contacto. `userId` falta si fue anónima. */
public record SuggestionResponse(UUID id, String title, UUID institutionId, String details, String contactEmail,
                                 UUID userId, Instant createdAt) {

    public static SuggestionResponse from(ProcedureSuggestion s) {
        return new SuggestionResponse(s.getId(), s.getTitle(), s.getInstitutionId(), s.getDetails(),
                s.getContactEmail(), s.getUserId(), s.getCreatedAt());
    }
}
