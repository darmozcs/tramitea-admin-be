package com.darmoz.tramiteaadmin.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Embeddable
public class ProcedureStepComment {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProcedureStepCommentLevel level;

    @Column(nullable = false)
    private String content;

    protected ProcedureStepComment() {
    }

    public ProcedureStepComment(ProcedureStepCommentLevel level, String content) {
        this.level = level;
        this.content = content;
    }

    public ProcedureStepCommentLevel getLevel() { return level; }
    public String getContent() { return content; }
}
