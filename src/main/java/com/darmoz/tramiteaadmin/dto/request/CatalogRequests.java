package com.darmoz.tramiteaadmin.dto.request;

import com.darmoz.tramiteaadmin.entity.ApplicationMethod;
import com.darmoz.tramiteaadmin.entity.AuthenticationLevel;
import com.darmoz.tramiteaadmin.entity.Currency;
import com.darmoz.tramiteaadmin.entity.ProcedureGroup;
import com.darmoz.tramiteaadmin.entity.ProcedureStepCommentLevel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Bodies de alta y edición (el mismo para POST y PUT). Los largos máximos
 * son los de las columnas (V1__init_schema.sql de tramitea-be) o los del
 * schema zod del front cuando son más cortos.
 */
public final class CatalogRequests {

    private CatalogRequests() {
    }

    /** Espejo de createCategory.schema.ts del admin. */
    public record SaveCategory(
            @NotBlank @Size(max = 40) String code,
            @NotBlank @Size(max = 120) String name,
            @Size(max = 500) String description,
            @NotBlank @Size(max = 60) String icon,
            @NotNull @Min(1) Integer displayOrder,
            @NotNull Boolean isActive) {
    }

    public record SaveInstitution(
            @NotBlank @Size(max = 50) String acronym,
            @NotBlank @Size(max = 255) String name,
            @Size(max = 255) String parentEntity,
            @NotBlank @Size(max = 500) String officialWebsite,
            @Size(max = 100) String contactPhone,
            @Email @Size(max = 255) String contactEmail,
            @Size(max = 500) String logoUrl,
            // País o provincia del país que administra el admin.
            @NotNull UUID locationId) {
    }

    /**
     * Requisitos, pasos y FAQs viajan completos: en cada guardado reemplazan a
     * los anteriores. Sin `slug` se genera a partir del nombre.
     */
    public record SaveProcedure(
            @NotBlank @Size(max = 255) String name,
            @NotNull ApplicationMethod applicationMethod,
            @NotNull AuthenticationLevel authenticationLevel,
            @NotNull ProcedureGroup group,
            @NotBlank String description,
            @NotBlank @Size(max = 500) String imageUrl,
            @NotNull UUID categoryId,
            @NotNull UUID institutionId,
            // País (trámite nacional) o provincia del país que administra el admin.
            @NotNull UUID locationId,
            boolean appointmentRequired,
            boolean isFree,
            @Valid Cost cost,
            @Valid Validity validity,
            @Size(max = 255) String estimatedDeliveryTime,
            @Valid Slug slug,
            @Min(0) Integer displayOrder,
            List<@Valid @NotNull Requirement> requirements,
            List<@Valid @NotNull Step> steps,
            List<@Valid @NotNull Faq> faqs) {
    }

    public record Cost(
            @NotNull @DecimalMin("0") BigDecimal amount,
            @NotNull Currency currency,
            @Size(max = 1000) String comments) {
    }

    public record Validity(
            @NotBlank @Size(max = 50) String value,
            @Size(max = 50) String unit,
            @Size(max = 1000) String comments) {
    }

    public record Slug(
            @Size(max = 255) String es,
            @Size(max = 255) String en) {
    }

    public record Requirement(
            @NotBlank @Size(max = 500) String title,
            @Size(max = 2000) String description,
            boolean isRequired,
            boolean isDigitallyVerifiable) {
    }

    public record Step(
            @NotBlank @Size(max = 500) String title,
            @NotBlank String instructions,
            @Size(max = 500) String channelUrl,
            @Size(max = 255) String estimatedTime,
            List<@Valid @NotNull StepComment> comments) {
    }

    public record StepComment(@NotNull ProcedureStepCommentLevel level, @NotBlank String content) {
    }

    public record Faq(
            @NotBlank @Size(max = 1000) String question,
            @NotBlank String answer) {
    }

    public record RejectSuggestion(@Size(max = 1000) String reason) {
    }

    public record UpdateLocation(@NotNull Boolean isActive) {
    }
}
