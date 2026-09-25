package at.papo.partnerportal.partner;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PartnerTradeProofReviewRequest(
        @NotBlank(message = "Nachweisversion ist erforderlich.")
        @Size(max = 36, message = "Nachweisversion ist ungültig.") String version,
        @NotNull(message = "Prüfstatus ist erforderlich.") PartnerTradeProofStatus status,
        @Size(max = 1000, message = "Ablehnungsgrund darf höchstens 1.000 Zeichen enthalten.") String rejectionReason
) {
}
