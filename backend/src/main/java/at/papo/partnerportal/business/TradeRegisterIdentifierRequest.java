package at.papo.partnerportal.business;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TradeRegisterIdentifierRequest(
        @NotBlank(message = "Registerland ist erforderlich.")
        @Size(min = 2, max = 2, message = "Registerland muss ein ISO-Code mit zwei Buchstaben sein.") String country,
        @NotNull(message = "Registerart ist erforderlich.") TradeRegisterIdentifierType type,
        @NotBlank(message = "Registerkennzeichen ist erforderlich.")
        @Size(max = 64, message = "Registerkennzeichen darf höchstens 64 Zeichen enthalten.") String value
) {
}
