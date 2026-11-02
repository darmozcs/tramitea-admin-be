package com.darmoz.tramiteaadmin.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "tr_requirements")
public class Requirement {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "procedure_id")
    private Procedure procedure;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(name = "is_required", nullable = false)
    private boolean required;

    @Column(name = "is_digitally_verifiable", nullable = false)
    private boolean digitallyVerifiable;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    protected Requirement() {
    }

    public Requirement(Procedure procedure, String title, String description, boolean required,
                       boolean digitallyVerifiable, int displayOrder) {
        this.id = UUID.randomUUID();
        this.procedure = procedure;
        this.title = title;
        this.description = description;
        this.required = required;
        this.digitallyVerifiable = digitallyVerifiable;
        this.displayOrder = displayOrder;
    }

    public UUID getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public boolean isRequired() { return required; }
    public boolean isDigitallyVerifiable() { return digitallyVerifiable; }
    public int getDisplayOrder() { return displayOrder; }
}
