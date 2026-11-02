package com.darmoz.tramiteaadmin.repository;

import com.darmoz.tramiteaadmin.entity.Procedure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface ProcedureRepository extends JpaRepository<Procedure, UUID>, JpaSpecificationExecutor<Procedure> {

    boolean existsByCategoryId(UUID categoryId);

    boolean existsByInstitutionId(UUID institutionId);

    /**
     * tramitea-be busca el detalle por slug en cualquiera de los dos idiomas,
     * así que un slug no puede repetirse ni cruzado entre columnas.
     */
    @Query("""
            select count(p) > 0 from Procedure p
            where p.id <> :id and (p.slugEs in (:slugEs, :slugEn) or p.slugEn in (:slugEs, :slugEn))""")
    boolean existsSlugOutside(UUID id, String slugEs, String slugEn);
}
