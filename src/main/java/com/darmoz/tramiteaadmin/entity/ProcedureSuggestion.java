package com.darmoz.tramiteaadmin.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Sugerencias que los ciudadanos mandan desde la web (las crea tramitea-be).
 * Acá solo se revisan: se aceptan (creando el trámite) o se rechazan.
 */
@Entity
@Table(name = "tr_procedure_suggestions")
public class ProcedureSuggestion {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(name = "institution_id", nullable = false)
    private UUID institutionId;

    @Column(nullable = false)
    private String details;

    @Column(name = "contact_email")
    private String contactEmail;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "location_id", nullable = false)
    private UUID locationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SuggestionStatus status;

    /** Id de darmoz-auth (sub del token) del admin que la revisó. */
    @Column(name = "reviewed_by")
    private UUID reviewedBy;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    /** Trámite creado al aceptarla. */
    @Column(name = "procedure_id")
    private UUID procedureId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected ProcedureSuggestion() {
    }

    public void accept(UUID adminAuthUserId, UUID procedureId) {
        review(SuggestionStatus.ACCEPTED, adminAuthUserId);
        this.procedureId = procedureId;
    }

    public void reject(UUID adminAuthUserId, String reason) {
        review(SuggestionStatus.REJECTED, adminAuthUserId);
        this.rejectionReason = reason;
    }

    private void review(SuggestionStatus status, UUID adminAuthUserId) {
        this.status = status;
        this.reviewedBy = adminAuthUserId;
        this.reviewedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public String getTitle() { return title; }
    public UUID getInstitutionId() { return institutionId; }
    public String getDetails() { return details; }
    public String getContactEmail() { return contactEmail; }
    public UUID getUserId() { return userId; }
    public UUID getLocationId() { return locationId; }
    public SuggestionStatus getStatus() { return status; }
    public UUID getReviewedBy() { return reviewedBy; }
    public Instant getReviewedAt() { return reviewedAt; }
    public String getRejectionReason() { return rejectionReason; }
    public UUID getProcedureId() { return procedureId; }
    public Instant getCreatedAt() { return createdAt; }
}
