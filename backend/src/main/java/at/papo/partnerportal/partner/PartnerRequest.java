package at.papo.partnerportal.partner;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

// Company-side create/edit — full record incl. commissionPercent and active
// (never exposed on the partner self-edit path, see PartnerContactRequest / FR-08).
public record PartnerRequest(
        @NotBlank(message = "Partnernummer ist erforderlich.") String partnerNumber,
        @NotBlank(message = "Name ist erforderlich.") String name,
        String address,
        String email,
        String phone,
        @NotNull(message = "Provision ist erforderlich.")
        @DecimalMin(value = "0.0", message = "Provision darf nicht negativ sein.") BigDecimal commissionPercent,
        Boolean active
) {
}
