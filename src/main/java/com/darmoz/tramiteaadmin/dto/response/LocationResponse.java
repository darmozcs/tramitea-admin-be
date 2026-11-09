package com.darmoz.tramiteaadmin.dto.response;

import com.darmoz.tramiteaadmin.entity.Location;
import com.darmoz.tramiteaadmin.entity.LocationType;

import java.util.UUID;

/** {@code parentId} es el país de una provincia; null en los países. */
public record LocationResponse(UUID id, LocationType type, String code, String name, UUID parentId,
                               boolean isActive) {

    public static LocationResponse from(Location l) {
        return new LocationResponse(l.getId(), l.getType(), l.getCode(), l.getName(), l.getParentId(),
                l.isActive());
    }
}
