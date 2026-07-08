package at.papo.partnerportal.customer;

import at.papo.partnerportal.common.AddressRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// Mandatory: last name + address. First name/e-mail/phone optional (customer-management spec).
public record KundeRequest(
        @NotBlank(message = "Nachname ist erforderlich.") String lastName,
        @NotNull(message = "Adresse ist erforderlich.") @Valid AddressRequest address,
        String firstName,
        String email,
        String phone
) {
}
