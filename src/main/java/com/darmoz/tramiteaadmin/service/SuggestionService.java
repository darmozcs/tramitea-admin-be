package com.darmoz.tramiteaadmin.service;

import com.darmoz.tramiteaadmin.dto.response.PaginatedResponse;
import com.darmoz.tramiteaadmin.dto.response.SuggestionResponse;
import com.darmoz.tramiteaadmin.repository.ProcedureSuggestionRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SuggestionService {

    private final ProcedureSuggestionRepository suggestionRepository;

    public SuggestionService(ProcedureSuggestionRepository suggestionRepository) {
        this.suggestionRepository = suggestionRepository;
    }

    /** Las más nuevas primero. */
    @Transactional(readOnly = true)
    public PaginatedResponse<SuggestionResponse> list(int page, int pageSize) {
        long total = suggestionRepository.count();
        Paging.Window window = Paging.window(total, page, pageSize,
                Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by("id")));
        List<SuggestionResponse> items = suggestionRepository.findAll(window.pageRequest())
                .map(SuggestionResponse::from)
                .getContent();
        return window.response(total, items);
    }
}
