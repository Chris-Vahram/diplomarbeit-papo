package at.papo.partnerportal.partner;

import at.papo.partnerportal.common.AddressRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// Partner self-edit: contact data only. No partnerNumber, commissionPercent, or
// active field — a partner cannot change those (FR-08).
public record PartnerContactRequest(
        @NotBlank(message = "Name ist erforderlich.") String name,
        @NotNull(message = "Adresse ist erforderlich.") @Valid AddressRequest address,
        String email,
        String phone
) {
}
