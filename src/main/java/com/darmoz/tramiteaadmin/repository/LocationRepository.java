package com.darmoz.tramiteaadmin.repository;

import com.darmoz.tramiteaadmin.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public interface LocationRepository extends JpaRepository<Location, UUID> {

    /** El país y sus provincias: primero el país, después las provincias por nombre. */
    @Query("select l from Location l where l.id = :countryId or l.parentId = :countryId order by l.type, l.name")
    List<Location> findCountryAndProvinces(UUID countryId);

    @Query("select l from Location l where l.type <> com.darmoz.tramiteaadmin.entity.LocationType.GLOBAL")
    List<Location> findAllExceptGlobal();

    /**
     * Alcance de un admin global: todos los países y provincias, sin la propia
     * GLOBAL. Países por nombre, cada uno seguido de sus provincias por nombre.
     */
    default List<Location> findAllCountriesAndProvinces() {
        List<Location> all = findAllExceptGlobal();
        Map<UUID, String> countryNames = all.stream()
                .filter(l -> l.getParentId() == null)
                .collect(Collectors.toMap(Location::getId, Location::getName));
        return all.stream()
                .sorted(Comparator.<Location, String>comparing(l -> countryNames.getOrDefault(l.getCountryId(), ""))
                        .thenComparing(Location::getCountryId)
                        .thenComparing(l -> l.getParentId() != null)
                        .thenComparing(Location::getName))
                .toList();
    }
}
