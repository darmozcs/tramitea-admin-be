package com.darmoz.tramiteaadmin.repository;

import com.darmoz.tramiteaadmin.entity.ProcedureSuggestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface ProcedureSuggestionRepository extends JpaRepository<ProcedureSuggestion, UUID>,
        JpaSpecificationExecutor<ProcedureSuggestion> {

    boolean existsByInstitutionId(UUID institutionId);
}
