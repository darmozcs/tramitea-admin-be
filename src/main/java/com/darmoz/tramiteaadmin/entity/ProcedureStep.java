package com.darmoz.tramiteaadmin.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tr_procedure_steps")
public class ProcedureStep {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "procedure_id")
    private Procedure procedure;

    @Column(name = "step_number", nullable = false)
    private int stepNumber;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String instructions;

    @Column(name = "channel_url")
    private String channelUrl;

    @Column(name = "estimated_time")
    private String estimatedTime;

    @ElementCollection
    @CollectionTable(name = "tr_procedure_step_comments", joinColumns = @JoinColumn(name = "step_id"))
    @OrderColumn(name = "position")
    private List<ProcedureStepComment> comments = new ArrayList<>();

    protected ProcedureStep() {
    }

    public ProcedureStep(Procedure procedure, int stepNumber, String title, String instructions, String channelUrl,
                         String estimatedTime, List<ProcedureStepComment> comments) {
        this.id = UUID.randomUUID();
        this.procedure = procedure;
        this.stepNumber = stepNumber;
        this.title = title;
        this.instructions = instructions;
        this.channelUrl = channelUrl;
        this.estimatedTime = estimatedTime;
        this.comments = new ArrayList<>(comments);
    }

    public UUID getId() { return id; }
    public int getStepNumber() { return stepNumber; }
    public String getTitle() { return title; }
    public String getInstructions() { return instructions; }
    public String getChannelUrl() { return channelUrl; }
    public String getEstimatedTime() { return estimatedTime; }
    public List<ProcedureStepComment> getComments() { return comments; }
}
