package com.darmoz.tramiteaadmin.dto.response;

import com.darmoz.tramiteaadmin.entity.ApplicationMethod;
import com.darmoz.tramiteaadmin.entity.AuthenticationLevel;
import com.darmoz.tramiteaadmin.entity.Currency;
import com.darmoz.tramiteaadmin.entity.Procedure;
import com.darmoz.tramiteaadmin.entity.ProcedureFaq;
import com.darmoz.tramiteaadmin.entity.ProcedureGroup;
import com.darmoz.tramiteaadmin.entity.ProcedureStep;
import com.darmoz.tramiteaadmin.entity.ProcedureStepCommentLevel;
import com.darmoz.tramiteaadmin.entity.Requirement;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Misma forma que el ProcedureResponse de tramitea-be (y el `Procedure` del
 * front), más displayOrder, que el admin edita.
 */
public record ProcedureResponse(
        UUID id,
        String name,
        ApplicationMethod applicationMethod,
        AuthenticationLevel authenticationLevel,
        ProcedureGroup group,
        String description,
        String imageUrl,
        UUID categoryId,
        UUID institutionId,
        boolean appointmentRequired,
        boolean isFree,
        Cost cost,
        Validity validity,
        String estimatedDeliveryTime,
        long viewCount,
        LocalizedValue slug,
        List<RequirementResponse> requirements,
        List<StepResponse> steps,
        List<FaqResponse> faqs,
        int displayOrder,
        Instant createdAt,
        Instant updatedAt) {

    public static ProcedureResponse from(Procedure p) {
        Cost cost = p.getCostAmount() == null ? null
                : new Cost(p.getCostAmount().doubleValue(), p.getCostCurrency(), p.getCostComments());
        Validity validity = p.getValidityValue() == null ? null
                : new Validity(p.getValidityValue(), p.getValidityUnit(), p.getValidityComments());

        return new ProcedureResponse(
                p.getId(), p.getName(), p.getApplicationMethod(), p.getAuthenticationLevel(), p.getGroup(),
                p.getDescription(), p.getImageUrl(), p.getCategoryId(), p.getInstitutionId(),
                p.isAppointmentRequired(), p.isFree(), cost, validity, p.getEstimatedDeliveryTime(),
                p.getViewCount(), new LocalizedValue(p.getSlugEs(), p.getSlugEn()),
                p.getRequirements().stream().map(r -> RequirementResponse.from(p.getId(), r)).toList(),
                p.getSteps().stream().map(s -> StepResponse.from(p.getId(), s)).toList(),
                p.getFaqs().stream().map(f -> FaqResponse.from(p.getId(), f)).toList(),
                p.getDisplayOrder(), p.getCreatedAt(), p.getUpdatedAt());
    }

    public record Cost(double amount, Currency currency, String comments) {
    }

    public record Validity(String value, String unit, String comments) {
    }

    public record LocalizedValue(String es, String en) {
    }

    public record RequirementResponse(UUID id, UUID procedureId, String title, String description,
                                      boolean isRequired, boolean isDigitallyVerifiable, int displayOrder) {

        static RequirementResponse from(UUID procedureId, Requirement r) {
            return new RequirementResponse(r.getId(), procedureId, r.getTitle(), r.getDescription(),
                    r.isRequired(), r.isDigitallyVerifiable(), r.getDisplayOrder());
        }
    }

    public record StepResponse(UUID id, UUID procedureId, int stepNumber, String title, String instructions,
                               String channelUrl, String estimatedTime, List<StepComment> comments) {

        static StepResponse from(UUID procedureId, ProcedureStep s) {
            // `comments?` es opcional en el contrato: se omite en vez de mandar [].
            List<StepComment> comments = s.getComments().isEmpty() ? null
                    : s.getComments().stream().map(c -> new StepComment(c.getLevel(), c.getContent())).toList();
            return new StepResponse(s.getId(), procedureId, s.getStepNumber(), s.getTitle(), s.getInstructions(),
                    s.getChannelUrl(), s.getEstimatedTime(), comments);
        }
    }

    public record StepComment(ProcedureStepCommentLevel level, String content) {
    }

    public record FaqResponse(UUID id, UUID procedureId, String question, String answer, int displayOrder,
                              Instant updatedAt) {

        static FaqResponse from(UUID procedureId, ProcedureFaq f) {
            return new FaqResponse(f.getId(), procedureId, f.getQuestion(), f.getAnswer(), f.getDisplayOrder(),
                    f.getUpdatedAt());
        }
    }
}
