package com.darmoz.tramiteaadmin.repository;

import com.darmoz.tramiteaadmin.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface LocationRepository extends JpaRepository<Location, UUID> {

    /** El país y sus provincias: primero el país, después las provincias por nombre. */
    @Query("select l from Location l where l.id = :countryId or l.parentId = :countryId order by l.type, l.name")
    List<Location> findCountryAndProvinces(UUID countryId);
}
