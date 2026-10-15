package at.papo.partnerportal.partner;

import at.papo.partnerportal.common.AddressRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// Partner self-edit: contact data only. No partnerNumber, commissionPercent, or
// active field — a partner cannot change those (FR-08).
public record PartnerContactRequest(
        @NotBlank(message = "Name ist erforderlich.") String name,
        @NotNull(message = "Adresse ist erforderlich.") @Valid AddressRequest address,
        @Email(message = "E-Mail-Adresse ist ungültig.") @Size(max = 254, message = "E-Mail-Adresse ist zu lang.") String email,
        String phone
) {
}
