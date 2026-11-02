package com.darmoz.tramiteaadmin.dto.response;

import java.util.List;

/** Espejo de `PaginatedResponse<T>` del front (currentPage es 1-based). */
public record PaginatedResponse<T>(long total, int currentPage, int totalPages, List<T> items) {
}
