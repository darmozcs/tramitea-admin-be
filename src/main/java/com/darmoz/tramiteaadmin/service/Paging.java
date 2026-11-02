package com.darmoz.tramiteaadmin.service;

import com.darmoz.tramiteaadmin.dto.response.PaginatedResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;

/**
 * Misma semántica que tramitea-be: `page` empieza en 1 y una página fuera de
 * rango se ajusta a la última (o a la primera) en vez de devolver vacío.
 */
final class Paging {

    static final int MAX_PAGE_SIZE = 100;

    private Paging() {
    }

    record Window(int currentPage, int totalPages, PageRequest pageRequest) {

        <T> PaginatedResponse<T> response(long total, List<T> items) {
            return new PaginatedResponse<>(total, currentPage, totalPages, items);
        }
    }

    static Window window(long total, int page, int pageSize, Sort sort) {
        int size = Math.clamp(pageSize, 1, MAX_PAGE_SIZE);
        int totalPages = (int) Math.max(1, (total + size - 1) / size);
        int currentPage = Math.clamp(page, 1, totalPages);
        return new Window(currentPage, totalPages, PageRequest.of(currentPage - 1, size, sort));
    }
}
