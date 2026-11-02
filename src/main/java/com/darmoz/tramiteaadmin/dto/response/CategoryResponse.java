package com.darmoz.tramiteaadmin.dto.response;

import com.darmoz.tramiteaadmin.entity.Category;

import java.util.UUID;

public record CategoryResponse(UUID id, String code, String name, String description, String icon,
                               int displayOrder, boolean isActive) {

    public static CategoryResponse from(Category c) {
        return new CategoryResponse(c.getId(), c.getCode(), c.getName(), c.getDescription(), c.getIcon(),
                c.getDisplayOrder(), c.isActive());
    }
}
