package com.darmoz.tramiteaadmin.repository;

import com.darmoz.tramiteaadmin.entity.Institution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface InstitutionRepository extends JpaRepository<Institution, UUID>, JpaSpecificationExecutor<Institution> {
}
