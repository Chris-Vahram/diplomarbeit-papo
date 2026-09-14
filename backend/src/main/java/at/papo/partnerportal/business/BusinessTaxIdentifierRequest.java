package at.papo.partnerportal.business;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record BusinessTaxIdentifierRequest(
        @NotBlank(message = "Steuerland ist erforderlich.") @Size(min = 2, max = 2, message = "Steuerland muss ein ISO-Code mit zwei Buchstaben sein.") String country,
        @NotNull(message = "Steuerart ist erforderlich.") BusinessTaxIdentifierType type,
        @NotBlank(message = "Steueridentifikationswert ist erforderlich.") @Size(max = 64, message = "Steueridentifikationswert darf höchstens 64 Zeichen enthalten.") String value
) {
}
