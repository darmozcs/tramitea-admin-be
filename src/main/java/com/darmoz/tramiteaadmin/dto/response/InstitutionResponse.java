package com.darmoz.tramiteaadmin.dto.response;

import com.darmoz.tramiteaadmin.entity.Institution;

import java.time.Instant;
import java.util.UUID;

public record InstitutionResponse(UUID id, String acronym, String name, String parentEntity, String officialWebsite,
                                  String contactPhone, String contactEmail, String logoUrl, Instant createdAt,
                                  Instant updatedAt) {

    public static InstitutionResponse from(Institution i) {
        return new InstitutionResponse(i.getId(), i.getAcronym(), i.getName(), i.getParentEntity(),
                i.getOfficialWebsite(), i.getContactPhone(), i.getContactEmail(), i.getLogoUrl(),
                i.getCreatedAt(), i.getUpdatedAt());
    }
}
