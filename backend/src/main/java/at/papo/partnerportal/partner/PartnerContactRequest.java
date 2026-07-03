package at.papo.partnerportal.partner;

import jakarta.validation.constraints.NotBlank;

// Partner self-edit: contact data only. No partnerNumber, commissionPercent, or
// active field — a partner cannot change those (FR-08).
public record PartnerContactRequest(
        @NotBlank(message = "Name ist erforderlich.") String name,
        String address,
        String email,
        String phone
) {
}
