package at.papo.partnerportal.business;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CompanyProfileRequest(
        @NotBlank(message = "Firmenname ist erforderlich.")
        @Size(max = 255, message = "Firmenname darf höchstens 255 Zeichen lang sein.") String businessName,
        @NotNull(message = "Steueridentifikation ist erforderlich.") @Valid BusinessTaxIdentifierRequest taxIdentifier,
        @NotNull(message = "Registerkennzeichen ist erforderlich.") @Valid TradeRegisterIdentifierRequest tradeRegisterIdentifier,
        @Email(message = "E-Mail für Bestellhinweise ist ungültig.")
        @Size(max = 254, message = "E-Mail für Bestellhinweise darf höchstens 254 Zeichen lang sein.") String orderNoticeEmail
) {
}
