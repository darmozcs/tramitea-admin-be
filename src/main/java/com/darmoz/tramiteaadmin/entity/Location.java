package com.darmoz.tramiteaadmin.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * Misma tabla que tramitea-be (tr_locations, V4__locations.sql). Los países
 * y provincias se cargan por migración; desde el admin solo se habilitan o
 * deshabilitan.
 */
@Entity
@Table(name = "tr_locations")
public class Location {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LocationType type;

    @Column(name = "parent_id")
    private UUID parentId;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    protected Location() {
    }

    /** Id del país: el propio si es un país, el del padre si es una provincia. */
    public UUID getCountryId() {
        return parentId == null ? id : parentId;
    }

    public void changeActive(boolean active) {
        this.active = active;
    }

    public UUID getId() { return id; }
    public LocationType getType() { return type; }
    public UUID getParentId() { return parentId; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public boolean isActive() { return active; }
}
