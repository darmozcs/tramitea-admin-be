package com.darmoz.tramiteaadmin.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

/** Misma tabla que tramitea-be (tr_categories). */
@Entity
@Table(name = "tr_categories")
public class Category {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private String icon;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    protected Category() {
    }

    public Category(UUID id) {
        this.id = id;
    }

    public void update(String code, String name, String description, String icon, int displayOrder, boolean active) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.displayOrder = displayOrder;
        this.active = active;
    }

    public UUID getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getIcon() { return icon; }
    public int getDisplayOrder() { return displayOrder; }
    public boolean isActive() { return active; }
}
