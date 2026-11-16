package com.darmoz.tramiteaadmin.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Misma tabla que tramitea-be (tr_procedures). Requisitos, pasos y FAQs se
 * editan junto con el trámite: en cada guardado se reemplazan enteros.
 */
@Entity
@Table(name = "tr_procedures")
public class Procedure {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "application_method", nullable = false)
    private ApplicationMethod applicationMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "authentication_level", nullable = false)
    private AuthenticationLevel authenticationLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "procedure_group", nullable = false)
    private ProcedureGroup group;

    @Column(nullable = false)
    private String description;

    @Column(name = "search_text", nullable = false)
    private String searchText;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "category_id", nullable = false)
    private UUID categoryId;

    @Column(name = "institution_id", nullable = false)
    private UUID institutionId;

    /** País (trámite nacional) o provincia. */
    @Column(name = "location_id", nullable = false)
    private UUID locationId;

    @Column(name = "appointment_required", nullable = false)
    private boolean appointmentRequired;

    @Column(name = "is_free", nullable = false)
    private boolean free;

    @Column(name = "cost_amount")
    private BigDecimal costAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "cost_currency")
    private Currency costCurrency;

    @Column(name = "cost_comments")
    private String costComments;

    @Column(name = "validity_value")
    private String validityValue;

    @Column(name = "validity_unit")
    private String validityUnit;

    @Column(name = "validity_comments")
    private String validityComments;

    @Column(name = "estimated_delivery_time")
    private String estimatedDeliveryTime;

    @Column(name = "view_count", nullable = false)
    private long viewCount;

    @Column(name = "slug_es", nullable = false)
    private String slugEs;

    @Column(name = "slug_en", nullable = false)
    private String slugEn;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "procedure", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private List<Requirement> requirements = new ArrayList<>();

    @OneToMany(mappedBy = "procedure", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("stepNumber ASC")
    private List<ProcedureStep> steps = new ArrayList<>();

    @OneToMany(mappedBy = "procedure", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private List<ProcedureFaq> faqs = new ArrayList<>();

    protected Procedure() {
    }

    public Procedure(UUID id) {
        this.id = id;
        this.createdAt = Instant.now();
    }

    /** Campos propios del trámite (sin hijos). searchText y updatedAt se recalculan acá. */
    public void update(Details d, String searchText) {
        this.name = d.name();
        this.applicationMethod = d.applicationMethod();
        this.authenticationLevel = d.authenticationLevel();
        this.group = d.group();
        this.description = d.description();
        this.searchText = searchText;
        this.imageUrl = d.imageUrl();
        this.categoryId = d.categoryId();
        this.institutionId = d.institutionId();
        this.locationId = d.locationId();
        this.appointmentRequired = d.appointmentRequired();
        this.free = d.free();
        this.costAmount = d.costAmount();
        this.costCurrency = d.costCurrency();
        this.costComments = d.costComments();
        this.validityValue = d.validityValue();
        this.validityUnit = d.validityUnit();
        this.validityComments = d.validityComments();
        this.estimatedDeliveryTime = d.estimatedDeliveryTime();
        this.slugEs = d.slugEs();
        this.slugEn = d.slugEn();
        this.displayOrder = d.displayOrder();
        this.updatedAt = Instant.now();
    }

    public void replaceChildren(List<Requirement> requirements, List<ProcedureStep> steps, List<ProcedureFaq> faqs) {
        this.requirements.clear();
        this.requirements.addAll(requirements);
        this.steps.clear();
        this.steps.addAll(steps);
        this.faqs.clear();
        this.faqs.addAll(faqs);
    }

    public record Details(String name, ApplicationMethod applicationMethod, AuthenticationLevel authenticationLevel,
                          ProcedureGroup group, String description, String imageUrl, UUID categoryId,
                          UUID institutionId, UUID locationId, boolean appointmentRequired, boolean free,
                          BigDecimal costAmount, Currency costCurrency, String costComments, String validityValue,
                          String validityUnit, String validityComments, String estimatedDeliveryTime,
                          String slugEs, String slugEn, int displayOrder) {
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public ApplicationMethod getApplicationMethod() { return applicationMethod; }
    public AuthenticationLevel getAuthenticationLevel() { return authenticationLevel; }
    public ProcedureGroup getGroup() { return group; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }
    public UUID getCategoryId() { return categoryId; }
    public UUID getInstitutionId() { return institutionId; }
    public UUID getLocationId() { return locationId; }
    public boolean isAppointmentRequired() { return appointmentRequired; }
    public boolean isFree() { return free; }
    public BigDecimal getCostAmount() { return costAmount; }
    public Currency getCostCurrency() { return costCurrency; }
    public String getCostComments() { return costComments; }
    public String getValidityValue() { return validityValue; }
    public String getValidityUnit() { return validityUnit; }
    public String getValidityComments() { return validityComments; }
    public String getEstimatedDeliveryTime() { return estimatedDeliveryTime; }
    public long getViewCount() { return viewCount; }
    public String getSlugEs() { return slugEs; }
    public String getSlugEn() { return slugEn; }
    public int getDisplayOrder() { return displayOrder; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public List<Requirement> getRequirements() { return requirements; }
    public List<ProcedureStep> getSteps() { return steps; }
    public List<ProcedureFaq> getFaqs() { return faqs; }
}
