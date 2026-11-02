package com.darmoz.tramiteaadmin.repository;

import com.darmoz.tramiteaadmin.entity.ProcedureSuggestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProcedureSuggestionRepository extends JpaRepository<ProcedureSuggestion, UUID> {

    boolean existsByInstitutionId(UUID institutionId);
}
