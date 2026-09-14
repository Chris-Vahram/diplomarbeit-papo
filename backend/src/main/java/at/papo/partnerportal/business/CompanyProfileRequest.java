package at.papo.partnerportal.business;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CompanyProfileRequest(
        @NotBlank(message = "Firmenname ist erforderlich.") String businessName,
        @NotNull(message = "Steueridentifikation ist erforderlich.") @Valid BusinessTaxIdentifierRequest taxIdentifier
) {
}
