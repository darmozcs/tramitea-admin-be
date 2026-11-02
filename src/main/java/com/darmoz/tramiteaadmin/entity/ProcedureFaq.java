package com.darmoz.tramiteaadmin.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tr_procedure_faqs")
public class ProcedureFaq {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "procedure_id")
    private Procedure procedure;

    @Column(nullable = false)
    private String question;

    @Column(nullable = false)
    private String answer;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected ProcedureFaq() {
    }

    public ProcedureFaq(Procedure procedure, String question, String answer, int displayOrder) {
        this.id = UUID.randomUUID();
        this.procedure = procedure;
        this.question = question;
        this.answer = answer;
        this.displayOrder = displayOrder;
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public String getQuestion() { return question; }
    public String getAnswer() { return answer; }
    public int getDisplayOrder() { return displayOrder; }
    public Instant getUpdatedAt() { return updatedAt; }
}
